package com.example.springrecipe.repository;

import com.example.springrecipe.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);

    @Query("SELECT CASE WHEN COUNT(ri) > 0 THEN true ELSE false END " +
            "FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    boolean isIngredientUsedInAnyRecipe(@Param("ingredientId") Long ingredientId);
}
