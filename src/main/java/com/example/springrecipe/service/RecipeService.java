package com.example.springrecipe.service;

import com.example.springrecipe.cache.RecipeCacheKey;
import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.dto.RecipeIngredientDTO;
import com.example.springrecipe.exceptions.CategoryNotFoundException;
import com.example.springrecipe.exceptions.IngredientNotFoundException;
import com.example.springrecipe.exceptions.RecipeNotFoundException;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.RecipeIngredient;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.CategoryRepository;
import com.example.springrecipe.repository.IngredientRepository;
import com.example.springrecipe.repository.RecipeIngredientRepository;
import com.example.springrecipe.repository.RecipeRepository;
import com.example.springrecipe.repository.UnitRepository;
import com.example.springrecipe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final UnitRepository unitRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeMapper mapper;

    private final Map<RecipeCacheKey, Page<RecipeDTO>> recipeCache = new ConcurrentHashMap<>();
    private final Map<RecipeCacheKey, Integer> cacheHitCount = new HashMap<>();
    private volatile boolean dataChanged = false;

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipesWithNPlusOneProblem() {
        return recipeRepository.findAll()
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipesWithEntityGraph() {
        return recipeRepository.findAllWithDetails()
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipesJPQL(String ingredientName, String categoryName, Pageable pageable) {
        RecipeCacheKey cacheKey = new RecipeCacheKey(
                ingredientName,
                categoryName,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString().contains(":")
                        ? pageable.getSort().toString().split(":")[0].trim()
                        : "name",
                pageable.getSort().toString().contains("DESC") ? "DESC" : "ASC"
        );
        log.info("Поиск рецептов с ключом: {}", cacheKey);

        if (recipeCache.containsKey(cacheKey)) {
            log.info("ДАННЫЕ НАЙДЕНЫ В КЭШЕ!");
            cacheHitCount.merge(cacheKey, 1, Integer::sum);
            return recipeCache.get(cacheKey);
        }

        log.info("ДАННЫХ НЕТ В КЭШЕ, выполняем запрос к БД");
        Page<Recipe> recipePage = recipeRepository.findByJPQL(ingredientName, categoryName, pageable);
        Page<RecipeDTO> resultPage = recipePage.map(mapper::toRecipeDTO);

        recipeCache.put(cacheKey, resultPage);
        log.info("Результат сохранен в кэш. Размер кэша: {}", recipeCache.size());

        return resultPage;
    }

    @Transactional(readOnly = true)
    public Page<RecipeDTO> searchRecipesNative(String ingredientName, String categoryName, Pageable pageable) {
        RecipeCacheKey cacheKey = new RecipeCacheKey(
                "native_" + ingredientName,
                categoryName,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString().contains(":")
                        ? pageable.getSort().toString().split(":")[0].trim()
                        : "name",
                pageable.getSort().toString().contains("DESC") ? "DESC" : "ASC"
        );

        if (recipeCache.containsKey(cacheKey)) {
            log.info("Native: данные найдены в кэше");
            return recipeCache.get(cacheKey);
        }

        Page<Recipe> recipePage = recipeRepository.findByNative(ingredientName, categoryName, pageable);
        Page<RecipeDTO> resultPage = recipePage.map(mapper::toRecipeDTO);

        recipeCache.put(cacheKey, resultPage);
        return resultPage;
    }

    private void invalidateCache() {
        log.warn("ИНВАЛИДАЦИЯ КЭША: очищаем {} записей", recipeCache.size());
        log.debug("Детали инвалидации: cacheKeys = {}", recipeCache.keySet());
        recipeCache.clear();
        cacheHitCount.clear();
        dataChanged = true;
        log.info("Кэш успешно инвалидирован");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCacheStatistics() {
        log.debug("Запрос статистики кэша");
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", recipeCache.size());
        stats.put("cacheKeys", recipeCache.keySet().stream()
                .map(key -> String.format(
                        "ingredient=%s, page=%d, size=%d, sort=%s %s",
                        key.getIngredientName(),
                        key.getPageNumber(),
                        key.getPageSize(),
                        key.getSortBy(),
                        key.getSortDirection()))
                .toList());
        stats.put("cacheHits", cacheHitCount.values().stream().mapToInt(Integer::intValue).sum());
        stats.put("hitDetails", cacheHitCount);
        stats.put("dataChanged", dataChanged);
        log.info("Статистика кэша: размер={}, хиты={}", recipeCache.size(),
                cacheHitCount.values().stream().mapToInt(Integer::intValue).sum());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getAllRecipes() {
        log.debug("Вызов метода getAllRecipes");

        List<RecipeDTO> result = recipeRepository.findAllWithDetails()
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();

        log.info("getAllRecipes: найдено {} рецептов", result.size());

        return result;
    }

    @Transactional(readOnly = true)
    public RecipeDTO getRecipeById(Long id) {
        log.debug("Поиск рецепта по ID: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Рецепт с ID {} не найден", id);
                    return new RecipeNotFoundException("Recipe not found with id: " + id);
                });

        log.info("Найден рецепт: {} (ID: {})", recipe.getName(), recipe.getId());
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByAuthorId(Long authorId) {
        log.debug("Поиск рецептов автора с ID: {}", authorId);

        List<RecipeDTO> result = recipeRepository.findByAuthorId(authorId)
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();

        log.info("Найдено {} рецептов для автора ID {}", result.size(), authorId);

        return result;
    }

    @Transactional(readOnly = true)
    public List<RecipeDTO> getRecipesByCategory(Long categoryId) {
        log.debug("Поиск рецептов в категории с ID: {}", categoryId);

        List<RecipeDTO> result = recipeRepository.findByCategoryId(categoryId)
                .stream()
                .map(mapper::toRecipeDTO)
                .toList();

        log.info("Найдено {} рецептов в категории ID {}", result.size(), categoryId);

        return result;
    }

    @Transactional
    public RecipeDTO createRecipe(RecipeDTO dto) {
        log.info("СОЗДАНИЕ НОВОГО РЕЦЕПТА: название='{}', авторId={}, категорияId={}",
                dto.getName(), dto.getAuthorId(), dto.getCategoryId());
        log.debug("Детали рецепта: {}", dto);

        try {
            RecipeDTO result = executeRecipeCreation(dto);

            log.info("Рецепт успешно создан: ID={}, название='{}'",
                    result.getId(), result.getName());

            invalidateCache();
            log.debug("Кэш инвалидирован после создания рецепта");

            return result;
        } catch (Exception e) {
            log.error("Ошибка при создании рецепта '{}': {}", dto.getName(), e.getMessage(), e);
            throw e;
        }
    }

    public RecipeDTO createRecipeWithoutTransaction(RecipeDTO dto) {
        return executeRecipeCreation(dto);
    }

    private RecipeDTO executeRecipeCreation(RecipeDTO dto) {
        log.debug("Поиск автора с ID: {}", dto.getAuthorId());
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> {
                    log.error("Автор с ID {} не найден", dto.getAuthorId());
                    return new UserNotFoundException("Author not found");
                });
        log.debug("Автор найден: {}", author.getUserName());

        log.debug("Поиск категории с ID: {}", dto.getCategoryId());
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Категория с ID {} не найдена", dto.getCategoryId());
                    return new CategoryNotFoundException("Category not found");
                });
        log.debug("Категория найдена: {}", category.getName());

        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());
        recipe.setAuthor(author);
        recipe.setCategory(category);
        recipe = recipeRepository.save(recipe);
        log.debug("Рецепт сохранен в БД с ID: {}", recipe.getId());

        if (dto.getRecipeIngredients() != null && !dto.getRecipeIngredients().isEmpty()) {
            log.debug("Обработка ингредиентов: {} шт", dto.getRecipeIngredients().size());
            Set<RecipeIngredient> recipeIngredients = new HashSet<>();

            for (RecipeIngredientDTO riDto : dto.getRecipeIngredients()) {
                if (riDto.getIngredientName() == null || riDto.getIngredientName().isEmpty()) {
                    log.warn("Пропущен ингредиент с пустым названием");
                    throw new IllegalArgumentException("Ingredient name is required");
                }

                log.debug("Обработка ингредиента: {}", riDto.getIngredientName());
                UnitOfMeasure unit = (riDto.getUnitAbbreviation() == null || riDto.getUnitAbbreviation().isEmpty())
                        ? null
                        : unitRepository.findByAbbreviation(riDto.getUnitAbbreviation())
                        .orElseThrow(() -> {
                            log.error("Единица измерения '{}' не найдена", riDto.getUnitAbbreviation());
                            return new UnitNotFoundException("Unit not found: " + riDto.getUnitAbbreviation());
                        });

                Ingredient ingredient = ingredientRepository.findByName(riDto.getIngredientName())
                        .orElseGet(() -> {
                            log.info("Создание нового ингредиента: {}", riDto.getIngredientName());
                            Ingredient newIng = new Ingredient();
                            newIng.setName(riDto.getIngredientName());
                            newIng.setUnit(unit);
                            return ingredientRepository.save(newIng);
                        });

                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(riDto.getQuantity());
                recipeIngredient.setUnit(unit);

                recipeIngredients.add(recipeIngredient);
                log.debug("Ингредиент '{}' добавлен в количестве {}", ingredient.getName(), riDto.getQuantity());
            }

            recipeIngredientRepository.saveAll(recipeIngredients);
            recipe.setRecipeIngredients(recipeIngredients);
            log.debug("Сохранено {} ингредиентов для рецепта", recipeIngredients.size());
        }

        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public RecipeDTO updateRecipe(Long id, RecipeDTO dto) {
        log.info("ОБНОВЛЕНИЕ РЕЦЕПТА: ID={}, название='{}'", id, dto.getName());
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Рецепт с ID {} не найден для обновления", id);
                    return new RecipeNotFoundException("Recipe not found");
                });

        String oldName = recipe.getName();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setCookingTime(dto.getCookingTime());
        log.debug("Основные поля обновлены: название '{}' -> '{}'", oldName, dto.getName());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
            recipe.setCategory(category);
            log.debug("Обновлена категория: {}", category.getName());
        }

        if (dto.getRecipeIngredients() != null && !dto.getRecipeIngredients().isEmpty()) {
            recipeIngredientRepository.deleteAll(recipe.getRecipeIngredients());

            Set<RecipeIngredient> recipeIngredients = new HashSet<>();

            for (RecipeIngredientDTO riDto : dto.getRecipeIngredients()) {
                Ingredient ingredient = ingredientRepository.findById(riDto.getIngredientId())
                        .orElseThrow(() -> new IngredientNotFoundException(
                                "Ingredient not found with id: " + riDto.getIngredientId()));

                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.setRecipe(recipe);
                recipeIngredient.setIngredient(ingredient);
                recipeIngredient.setQuantity(riDto.getQuantity());

                recipeIngredients.add(recipeIngredient);
            }

            recipeIngredientRepository.saveAll(recipeIngredients);
            recipe.setRecipeIngredients(recipeIngredients);
            log.debug("Добавлено {} новых ингредиентов", recipeIngredients.size());
        }

        recipe = recipeRepository.save(recipe);
        log.info("Рецепт ID={} успешно обновлен", id);
        invalidateCache();
        log.debug("Кэш инвалидирован после обновления");
        return mapper.toRecipeDTO(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        log.warn("УДАЛЕНИЕ РЕЦЕПТА: ID={}", id);

        try {
            if (!recipeRepository.existsById(id)) {
                log.warn("Попытка удалить несуществующий рецепт с ID={}", id);
                throw new RecipeNotFoundException("Recipe not found");
            }
            recipeRepository.deleteById(id);

            log.info("Рецепт ID={} успешно удален", id);
            invalidateCache();
            log.debug("Кэш инвалидирован после удаления");
        } catch (Exception e) {
            log.error("Ошибка при удалении рецепта ID={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}
