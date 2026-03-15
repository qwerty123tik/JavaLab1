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
            "LEFT JOIN r.recipeIngredients ri " +
            "LEFT JOIN ri.ingredient i " +
            "WHERE " +
            "(:ingredientName IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))) AND " +
            "(:categoryName IS NULL OR LOWER(r.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%')))")
    Page<Recipe> findByJPQL(@Param("ingredientName") String ingredientName,
                            @Param("categoryName") String categoryName,
                            Pageable pageable);

    @Query(value = "SELECT DISTINCT r.* FROM recipes r " +
            "LEFT JOIN recipe_ingredients ri ON r.id = ri.recipe_id " +
            "LEFT JOIN ingredients i ON ri.ingredient_id = i.id " +
            "LEFT JOIN categories c ON r.category_id = c.id " +
            "WHERE " +
            "(:ingredientName IS NULL OR CAST(i.name AS TEXT) ILIKE '%' || CAST(:ingredientName AS TEXT) || '%') AND " +
            "(:categoryName IS NULL OR CAST(c.name AS TEXT) ILIKE '%' || CAST(:categoryName AS TEXT) || '%')",
            countQuery = "SELECT COUNT(DISTINCT r.id) FROM recipes r " +
                    "LEFT JOIN recipe_ingredients ri ON r.id = ri.recipe_id " +
                    "LEFT JOIN ingredients i ON ri.ingredient_id = i.id " +
                    "LEFT JOIN categories c ON r.category_id = c.id " +
                    "WHERE " +
                    "(:ingredientName IS NULL OR CAST(i.name AS TEXT) ILIKE '%' " +
                    "|| CAST(:ingredientName AS TEXT) || '%') AND " +
                    "(:categoryName IS NULL OR CAST(c.name AS TEXT) ILIKE '%' || CAST(:categoryName AS TEXT) || '%')",
            nativeQuery = true)
    Page<Recipe> findByNative(@Param("ingredientName") String ingredientName,
                              @Param("categoryName") String categoryName,
                              Pageable pageable);
}
