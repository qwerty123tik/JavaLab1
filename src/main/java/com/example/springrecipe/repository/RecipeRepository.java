package com.example.springrecipe.repository;

import com.example.springrecipe.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByAuthorId(Long authorId);

    List<Recipe> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = { "category", "author", "recipeIngredients", "recipeIngredients.ingredient",
                                    "recipeIngredients.ingredient.unit", "reviews"})
    @Query("SELECT DISTINCT  r FROM Recipe r")
    List<Recipe> findAllWithDetails();

    @EntityGraph(attributePaths = {"category", "author", "recipeIngredients", "recipeIngredients.ingredient",
                                   "recipeIngredients.ingredient.unit", "reviews"})
    Optional<Recipe> findById(Long id);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "JOIN r.recipeIngredients ri " +
            "JOIN ri.ingredient i " +
            "WHERE (:ingredientName IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%')))")
    Page<Recipe> findByIngredientNameJPQL(@Param("ingredientName") String ingredientName, Pageable pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipes r " +
            "JOIN recipe_ingredients ri ON r.id = ri.recipe_id " +
            "JOIN ingredients i ON ri.ingredient_id = i.id " +
            "WHERE (:ingredientName IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%')))",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM recipes r " +
                    "JOIN recipe_ingredients ri ON r.id = ri.recipe_id " +
                    "JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "WHERE (:ingredientName IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%')))",
            nativeQuery = true)
    Page<Recipe> findByIngredientNameWithNative(@Param("ingredientName") String ingredientName, Pageable pageable);
}
