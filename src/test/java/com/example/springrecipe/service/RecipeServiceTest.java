package com.example.springrecipe.service;

import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.dto.RecipeIngredientDTO;
import com.example.springrecipe.exceptions.*;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.*;
import com.example.springrecipe.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private RecipeIngredientRepository recipeIngredientRepository;

    @Mock
    private RecipeMapper mapper;

    @InjectMocks
    private RecipeService recipeService;

    private User testUser;
    private Category testCategory;
    private UnitOfMeasure testUnit;
    private Ingredient testIngredient;
    private Recipe testRecipe;
    private RecipeDTO testRecipeDTO;
    private RecipeIngredientDTO testIngredientDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testUser");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Breakfast");

        testUnit = new UnitOfMeasure();
        testUnit.setId(1L);
        testUnit.setAbbreviation("g");
        testUnit.setName("gram");

        testIngredient = new Ingredient();
        testIngredient.setId(1L);
        testIngredient.setName("Flour");
        testIngredient.setUnit(testUnit);

        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setName("Pancakes");
        testRecipe.setDescription("Delicious pancakes");
        testRecipe.setCookingTime(30);
        testRecipe.setAuthor(testUser);
        testRecipe.setCategory(testCategory);
        testRecipe.setRecipeIngredients(new HashSet<>());

        testIngredientDTO = RecipeIngredientDTO.builder()
                .ingredientName("Flour")
                .quantity(250.0)
                .unitAbbreviation("g")
                .build();

        testRecipeDTO = RecipeDTO.builder()
                .id(1L)
                .name("Pancakes")
                .description("Delicious pancakes")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();
    }

    @Test
    void getAllRecipesWithNPlusOneProblem_shouldReturnRecipes() {
        when(recipeRepository.findAll()).thenReturn(List.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        List<RecipeDTO> result = recipeService.getAllRecipesWithNPlusOneProblem();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRecipeDTO);
        verify(recipeRepository).findAll();
        verify(mapper).toRecipeDTO(testRecipe);
    }

    @Test
    void getAllRecipesWithEntityGraph_shouldReturnRecipes() {
        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        List<RecipeDTO> result = recipeService.getAllRecipesWithEntityGraph();

        assertThat(result).hasSize(1);
        verify(recipeRepository).findAllWithDetails();
    }

    @Test
    void searchRecipesJPQL_cacheMiss_shouldQueryDbAndCache() {
        String ingredientName = "Flour";
        String categoryName = "Breakfast";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));
        Page<RecipeDTO> dtoPage = new PageImpl<>(List.of(testRecipeDTO));

        when(recipeRepository.findByJPQL(ingredientName, categoryName, pageable)).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL(ingredientName, categoryName, pageable);

        assertThat(result).isEqualTo(dtoPage);
        verify(recipeRepository).findByJPQL(ingredientName, categoryName, pageable);
    }

    @Test
    void searchRecipesJPQL_cacheHit_shouldReturnCached() {
        String ingredientName = "Flour";
        String categoryName = "Breakfast";
        Pageable pageable = PageRequest.of(0, 10);
        Page<RecipeDTO> dtoPage = new PageImpl<>(List.of(testRecipeDTO));

        when(recipeRepository.findByJPQL(ingredientName, categoryName, pageable)).thenReturn(new PageImpl<>(List.of(testRecipe)));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);
        recipeService.searchRecipesJPQL(ingredientName, categoryName, pageable); // populate cache

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL(ingredientName, categoryName, pageable);
        assertThat(result).isEqualTo(dtoPage);
        verify(recipeRepository, times(1)).findByJPQL(any(), any(), any());
    }

    @Test
    void searchRecipesJPQL_withNullParams_shouldWork() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));
        when(recipeRepository.findByJPQL(null, null, pageable)).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL(null, null, pageable);

        assertThat(result).isNotNull();
        verify(recipeRepository).findByJPQL(null, null, pageable);
    }

    @Test
    void searchRecipesJPQL_withAscendingSort_shouldCoverAscBranch() {
        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));

        when(recipeRepository.findByJPQL(any(), any(), eq(pageable))).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL("Flour", "Breakfast", pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void searchRecipesJPQL_withDescendingSort_shouldCoverDescBranch() {
        Sort sort = Sort.by("name").descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));

        when(recipeRepository.findByJPQL(any(), any(), eq(pageable))).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesJPQL("Flour", "Breakfast", pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void searchRecipesNative_cacheMiss_shouldQueryDbAndCache() {
        String ingredientName = "Flour";
        String categoryName = "Breakfast";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));
        when(recipeRepository.findByNative(ingredientName, categoryName, pageable)).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesNative(ingredientName, categoryName, pageable);

        assertThat(result).isNotNull();
        verify(recipeRepository).findByNative(ingredientName, categoryName, pageable);
    }

    @Test
    void searchRecipesNative_cacheHit_shouldReturnCached() {
        String ingredientName = "Flour";
        String categoryName = "Breakfast";
        Pageable pageable = PageRequest.of(0, 10);
        when(recipeRepository.findByNative(ingredientName, categoryName, pageable)).thenReturn(new PageImpl<>(List.of(testRecipe)));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);
        recipeService.searchRecipesNative(ingredientName, categoryName, pageable); // populate cache

        Page<RecipeDTO> result = recipeService.searchRecipesNative(ingredientName, categoryName, pageable);
        assertThat(result).isNotNull();
        verify(recipeRepository, times(1)).findByNative(any(), any(), any());
    }

    @Test
    void searchRecipesNative_withDescendingSort_shouldCoverDescBranch() {
        Sort sort = Sort.by("name").descending();
        Pageable pageable = PageRequest.of(0, 10, sort);
        Page<Recipe> recipePage = new PageImpl<>(List.of(testRecipe));

        when(recipeRepository.findByNative(any(), any(), eq(pageable))).thenReturn(recipePage);
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        Page<RecipeDTO> result = recipeService.searchRecipesNative("Flour", "Breakfast", pageable);
        assertThat(result).isNotNull();
    }

    @Test
    void getCacheStatistics_shouldReturnStats() {
        Pageable pageable = PageRequest.of(0, 10);
        when(recipeRepository.findByJPQL(any(), any(), any())).thenReturn(new PageImpl<>(List.of(testRecipe)));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);
        recipeService.searchRecipesJPQL("Flour", "Breakfast", pageable);
        Map<String, Object> stats = recipeService.getCacheStatistics();

        assertThat(stats)
                .containsEntry("cacheSize", 1)
                .containsEntry("cacheHits", 0)
                .containsEntry("dataChanged", false)
                .satisfies(map -> assertThat(map.get("cacheKeys")).isInstanceOf(List.class));
    }

    @Test
    void getAllRecipes_shouldReturnAllRecipes() {
        when(recipeRepository.findAllWithDetails()).thenReturn(List.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        List<RecipeDTO> result = recipeService.getAllRecipes();

        assertThat(result).hasSize(1);
        verify(recipeRepository).findAllWithDetails();
    }

    @Test
    void getRecipeById_existingId_shouldReturnRecipe() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.getRecipeById(1L);

        assertThat(result).isEqualTo(testRecipeDTO);
        verify(recipeRepository).findById(1L);
    }

    @Test
    void getRecipeById_nonExistingId_shouldThrowRecipeNotFoundException() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getRecipeById(99L))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessageContaining("Recipe not found with id: 99");
    }

    @Test
    void getRecipesByAuthorId_shouldReturnRecipes() {
        when(recipeRepository.findByAuthorId(1L)).thenReturn(List.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        List<RecipeDTO> result = recipeService.getRecipesByAuthorId(1L);

        assertThat(result).hasSize(1);
        verify(recipeRepository).findByAuthorId(1L);
    }

    @Test
    void getRecipesByCategory_shouldReturnRecipes() {
        when(recipeRepository.findByCategoryId(1L)).thenReturn(List.of(testRecipe));
        when(mapper.toRecipeDTO(testRecipe)).thenReturn(testRecipeDTO);

        List<RecipeDTO> result = recipeService.getRecipesByCategory(1L);

        assertThat(result).hasSize(1);
        verify(recipeRepository).findByCategoryId(1L);
    }

    @Test
    void createRecipe_success_shouldCreateAndInvalidateCache() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        RecipeDTO result = recipeService.createRecipe(testRecipeDTO);

        assertThat(result).isEqualTo(testRecipeDTO);
        verify(recipeRepository).save(any(Recipe.class));

        Map<String, Object> stats = recipeService.getCacheStatistics();
        assertThat(stats).containsEntry("cacheSize", 0);
    }

    @Test
    void createRecipe_authorNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.createRecipe(testRecipeDTO))
                .isInstanceOf(UserNotFoundException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_categoryNotFound_shouldThrowCategoryNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.createRecipe(testRecipeDTO))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void createRecipe_missingUnit_shouldThrowUnitNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.empty());
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        assertThatThrownBy(() -> recipeService.createRecipe(testRecipeDTO))
                .isInstanceOf(UnitNotFoundException.class)
                .hasMessageContaining("Unit not found: g");

        verify(unitRepository).findByAbbreviation("g");
        verify(ingredientRepository, never()).findByName(any());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void createRecipe_ingredientNameNull_shouldThrowIllegalArgumentException() {
        testIngredientDTO.setIngredientName(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        assertThatThrownBy(() -> recipeService.createRecipe(testRecipeDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingredient name is required");

        verify(ingredientRepository, never()).findByName(any());
        verify(unitRepository, never()).findByAbbreviation(any());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void createRecipe_withoutIngredients_shouldSucceed() {
        RecipeDTO dto = RecipeDTO.builder()
                .name("Empty Recipe")
                .description("No ingredients")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.createRecipe(dto);
        assertThat(result).isEqualTo(testRecipeDTO);
        verify(recipeIngredientRepository, never()).saveAll(anyCollection());
    }

    @Test
    void createRecipe_withNullCategoryId_shouldSucceed() {
        RecipeDTO dto = RecipeDTO.builder()
                .name("No Category")
                .description("No category assigned")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(null)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        RecipeDTO result = recipeService.createRecipe(dto);
        assertThat(result).isEqualTo(testRecipeDTO);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void createRecipe_withNullUnit_shouldSucceedAndSetUnitNull() {
        RecipeIngredientDTO ingredientWithNullUnit = RecipeIngredientDTO.builder()
                .ingredientName("Sugar")
                .quantity(100.0)
                .unitAbbreviation(null)
                .build();

        RecipeDTO dto = RecipeDTO.builder()
                .name("Sweet Pancakes")
                .description("Sweet version")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(ingredientWithNullUnit))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(ingredientRepository.findByName("Sugar")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(new Ingredient());
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        ArgumentCaptor<Collection<RecipeIngredient>> captor = ArgumentCaptor.forClass(Collection.class);
        when(recipeIngredientRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        RecipeDTO result = recipeService.createRecipe(dto);

        assertThat(result).isEqualTo(testRecipeDTO);
        Collection<RecipeIngredient> savedIngredients = captor.getValue();
        assertThat(savedIngredients).hasSize(1);
        assertThat(savedIngredients.iterator().next().getUnit()).isNull();
        verify(unitRepository, never()).findByAbbreviation(any());
    }

    @Test
    void createRecipe_withEmptyUnit_shouldSucceedAndSetUnitNull() {
        RecipeIngredientDTO ingredientWithEmptyUnit = RecipeIngredientDTO.builder()
                .ingredientName("Sugar")
                .quantity(100.0)
                .unitAbbreviation("")
                .build();

        RecipeDTO dto = RecipeDTO.builder()
                .name("Sweet Pancakes")
                .description("Sweet version")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(ingredientWithEmptyUnit))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(ingredientRepository.findByName("Sugar")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(new Ingredient());
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        ArgumentCaptor<Collection<RecipeIngredient>> captor = ArgumentCaptor.forClass(Collection.class);
        when(recipeIngredientRepository.saveAll(captor.capture())).thenReturn(Collections.emptyList());

        RecipeDTO result = recipeService.createRecipe(dto);

        assertThat(result).isEqualTo(testRecipeDTO);
        Collection<RecipeIngredient> savedIngredients = captor.getValue();
        assertThat(savedIngredients).hasSize(1);
        assertThat(savedIngredients.iterator().next().getUnit()).isNull();
        verify(unitRepository, never()).findByAbbreviation(any());
    }

    @Test
    void createRecipeWithoutTransaction_shouldWorkSameAsCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        RecipeDTO result = recipeService.createRecipeWithoutTransaction(testRecipeDTO);

        assertThat(result).isEqualTo(testRecipeDTO);
    }

    @Test
    void updateRecipe_success_shouldUpdateAndInvalidateCache() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated Pancakes")
                .description("Updated")
                .cookingTime(35)
                .categoryId(2L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        Category newCategory = new Category();
        newCategory.setId(2L);
        newCategory.setName("Dinner");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.of(testIngredient));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.updateRecipe(1L, updateDto);

        assertThat(result).isEqualTo(testRecipeDTO);
        verify(recipeRepository).save(testRecipe);

        Map<String, Object> stats = recipeService.getCacheStatistics();
        assertThat(stats).containsEntry("cacheSize", 0);
    }

    @Test
    void updateRecipe_recipeNotFound_shouldThrowRecipeNotFoundException() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.updateRecipe(99L, testRecipeDTO))
                .isInstanceOf(RecipeNotFoundException.class);
    }

    @Test
    void updateRecipe_withNullIngredients_shouldClearIngredients() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(null)
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        recipeService.updateRecipe(1L, updateDto);

        assertThat(testRecipe.getRecipeIngredients()).isEmpty();
    }

    @Test
    void updateRecipe_withEmptyIngredients_shouldClearIngredients() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(Collections.emptyList())
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        recipeService.updateRecipe(1L, updateDto);

        assertThat(testRecipe.getRecipeIngredients()).isEmpty();
    }

    @Test
    void updateRecipe_ingredientNotFound_createsNewIngredient() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated Pancakes")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.updateRecipe(1L, updateDto);

        assertThat(result).isEqualTo(testRecipeDTO);
        verify(ingredientRepository).save(any(Ingredient.class));
    }

    @Test
    void updateRecipe_withNullUnit_shouldSetUnitNull() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        RecipeIngredientDTO ingredientWithNullUnit = RecipeIngredientDTO.builder()
                .ingredientName("Flour")
                .quantity(250.0)
                .unitAbbreviation(null)
                .build();
        updateDto.setRecipeIngredients(List.of(ingredientWithNullUnit));

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.of(testIngredient));
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        when(recipeRepository.save(recipeCaptor.capture())).thenReturn(testRecipe);

        RecipeDTO result = recipeService.updateRecipe(1L, updateDto);

        assertThat(result).isEqualTo(testRecipeDTO);
        Recipe savedRecipe = recipeCaptor.getValue();
        assertThat(savedRecipe.getRecipeIngredients()).hasSize(1);
        RecipeIngredient savedIngredient = savedRecipe.getRecipeIngredients().iterator().next();
        assertThat(savedIngredient.getUnit()).isNull();
        verify(unitRepository, never()).findByAbbreviation(any());
    }

    @Test
    void updateRecipe_withEmptyUnit_shouldSetUnitNull() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        RecipeIngredientDTO ingredientWithEmptyUnit = RecipeIngredientDTO.builder()
                .ingredientName("Flour")
                .quantity(250.0)
                .unitAbbreviation("")
                .build();
        updateDto.setRecipeIngredients(List.of(ingredientWithEmptyUnit));

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.of(testIngredient));
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        when(recipeRepository.save(recipeCaptor.capture())).thenReturn(testRecipe);

        RecipeDTO result = recipeService.updateRecipe(1L, updateDto);

        assertThat(result).isEqualTo(testRecipeDTO);
        Recipe savedRecipe = recipeCaptor.getValue();
        assertThat(savedRecipe.getRecipeIngredients()).hasSize(1);
        RecipeIngredient savedIngredient = savedRecipe.getRecipeIngredients().iterator().next();
        assertThat(savedIngredient.getUnit()).isNull();
        verify(unitRepository, never()).findByAbbreviation(any());
    }

    @Test
    void updateRecipe_withNullCategoryId_shouldSucceed() {
        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(null)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.of(testIngredient));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.updateRecipe(1L, updateDto);
        assertThat(result).isEqualTo(testRecipeDTO);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void deleteRecipe_success_shouldDeleteAndInvalidateCache() {
        when(recipeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(recipeRepository).deleteById(1L);

        recipeService.deleteRecipe(1L);

        verify(recipeRepository).deleteById(1L);

        Map<String, Object> stats = recipeService.getCacheStatistics();
        assertThat(stats).containsEntry("cacheSize", 0);
    }

    @Test
    void deleteRecipe_notFound_shouldThrowRecipeNotFoundException() {
        when(recipeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> recipeService.deleteRecipe(99L))
                .isInstanceOf(RecipeNotFoundException.class);
        verify(recipeRepository, never()).deleteById(any());
    }

    @Test
    void bulkCreateRecipesWithTransaction_success_shouldCreateAll() {
        List<RecipeDTO> dtos = List.of(testRecipeDTO, testRecipeDTO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithTransaction(dtos);

        assertThat(result).hasSize(2);
        verify(recipeRepository, times(2)).save(any(Recipe.class));
        verify(recipeIngredientRepository, times(2)).saveAll(anyCollection());
    }

    @Test
    void bulkCreateRecipesWithTransaction_invalidDto_shouldThrow() {
        RecipeDTO invalidDto = RecipeDTO.builder().name(null).build();
        List<RecipeDTO> dtos = List.of(testRecipeDTO, invalidDto);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> recipeService.bulkCreateRecipesWithTransaction(dtos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("В списке передан невалидный рецепт (пустое имя)");

        verify(recipeRepository, atLeastOnce()).save(any(Recipe.class));
    }

    @Test
    void bulkCreateRecipesWithoutTransaction_partialSuccess_shouldCreateValidOnes() {
        RecipeDTO validDto = testRecipeDTO;
        RecipeDTO invalidDto = RecipeDTO.builder().name(null).build();
        List<RecipeDTO> dtos = List.of(validDto, invalidDto);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithoutTransaction(dtos);

        assertThat(result).hasSize(1);
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    void bulkCreateRecipesWithoutTransaction_allValid_shouldCreateAll() {
        List<RecipeDTO> dtos = List.of(testRecipeDTO, testRecipeDTO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithoutTransaction(dtos);

        assertThat(result).hasSize(2);
        verify(recipeRepository, times(2)).save(any(Recipe.class));
    }

    @Test
    void bulkCreateRecipesWithoutTransaction_allInvalid_shouldCreateNoneAndNotInvalidateCache() {
        RecipeDTO blankNameDto = RecipeDTO.builder()
                .name("")
                .description("Blank name")
                .cookingTime(10)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();
        RecipeDTO nullNameDto = RecipeDTO.builder()
                .name(null)
                .description("Null name")
                .cookingTime(10)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(testIngredientDTO))
                .build();
        List<RecipeDTO> dtos = List.of(blankNameDto, nullNameDto);

        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithoutTransaction(dtos);

        assertThat(result).isEmpty();
        verify(recipeRepository, never()).save(any(Recipe.class));
        verify(recipeIngredientRepository, never()).saveAll(anyCollection());

        Map<String, Object> stats = recipeService.getCacheStatistics();
        assertThat(stats)
                .containsEntry("cacheSize", 0)
                .containsEntry("dataChanged", false);
    }

    @Test
    void createRecipe_withEmptyIngredientsList_shouldSucceed() {
        RecipeDTO dto = RecipeDTO.builder()
                .name("Empty Ingredients")
                .description("No ingredients")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(Collections.emptyList())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);

        RecipeDTO result = recipeService.createRecipe(dto);
        assertThat(result).isEqualTo(testRecipeDTO);
        verify(recipeIngredientRepository, never()).saveAll(anyCollection());
    }

    @Test
    void createRecipe_withEmptyIngredientName_shouldThrowIllegalArgumentException() {
        RecipeIngredientDTO invalidIngredient = RecipeIngredientDTO.builder()
                .ingredientName("")
                .quantity(100.0)
                .unitAbbreviation("g")
                .build();

        RecipeDTO dto = RecipeDTO.builder()
                .name("Invalid")
                .description("Invalid ingredient name")
                .cookingTime(30)
                .authorId(1L)
                .categoryId(1L)
                .recipeIngredients(List.of(invalidIngredient))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);

        assertThatThrownBy(() -> recipeService.createRecipe(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingredient name is required");

        verify(recipeIngredientRepository, never()).saveAll(anyCollection());
    }

    @Test
    void updateRecipe_withEmptyIngredientName_shouldThrowIllegalArgumentException() {
        RecipeIngredientDTO invalidIngredient = RecipeIngredientDTO.builder()
                .ingredientName("")
                .quantity(100.0)
                .unitAbbreviation("g")
                .build();

        RecipeDTO updateDto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(35)
                .categoryId(1L)
                .recipeIngredients(List.of(invalidIngredient))
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        assertThatThrownBy(() -> recipeService.updateRecipe(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingredient name is required");

        verify(recipeIngredientRepository, never()).saveAll(anyCollection());
    }

    @Test
    void bulkCreateRecipesWithoutTransaction_withNullElementInList_shouldHandleGracefully() {
        RecipeDTO validDto = testRecipeDTO;
        List<RecipeDTO> dtos = Arrays.asList(validDto, null, validDto);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(unitRepository.findByAbbreviation("g")).thenReturn(Optional.of(testUnit));
        when(ingredientRepository.findByName("Flour")).thenReturn(Optional.empty());
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(testIngredient);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(testRecipe);
        when(mapper.toRecipeDTO(any(Recipe.class))).thenReturn(testRecipeDTO);
        when(recipeIngredientRepository.saveAll(anyCollection())).thenReturn(Collections.emptyList());

        List<RecipeDTO> result = recipeService.bulkCreateRecipesWithoutTransaction(dtos);

        assertThat(result).hasSize(2);
        verify(recipeRepository, times(2)).save(any(Recipe.class));
    }

    @Test
    void updateRecipe_withNullIngredientName_shouldThrowIllegalArgumentException() {

        RecipeIngredientDTO ingredient = RecipeIngredientDTO.builder()
                .ingredientName(null)
                .quantity(100.0)
                .unitAbbreviation("g")
                .build();

        RecipeDTO dto = RecipeDTO.builder()
                .name("Updated")
                .description("Updated")
                .cookingTime(30)
                .categoryId(1L)
                .recipeIngredients(List.of(ingredient))
                .build();

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(testRecipe));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        assertThatThrownBy(() ->
                recipeService.updateRecipe(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingredient name is required");
    }

}