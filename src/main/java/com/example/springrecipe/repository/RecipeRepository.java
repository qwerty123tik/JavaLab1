package com.example.springrecipe.repository;

import com.example.springrecipe.model.Recipe;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class RecipeRepository {
    private final ConcurrentHashMap<Long, Recipe> recipes = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Recipe save(Recipe recipe) {
        if (recipe.getId() == null) {
            recipe.setId(idGenerator.getAndIncrement());
        }
        recipes.put(recipe.getId(), recipe);
        return recipe;
    }

    public Optional<Recipe> findById(Long id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public List<Recipe> findAll() {
        return new ArrayList<>(recipes.values());
    }

    public List<Recipe> findByDifficulty(String difficulty) {
        return recipes.values().stream()
                .filter(recipe -> recipe.getDifficulty().equalsIgnoreCase(difficulty))
                .toList();
    }

    public void deleteById(Long id) {
        recipes.remove(id);
    }
}
