package com.example.springrecipe.repository;

import com.example.springrecipe.model.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByAuthorId(Long authorId);

    List<Recipe> findByCategoryId(Long categoryId);

    List<Recipe> findByName(String name);

    @EntityGraph(attributePaths = {"category", "author", "ingredients"})
    @Query("SELECT r FROM Recipe r")
    List<Recipe> findAllWithDetails();

    @EntityGraph(attributePaths = {"category", "author", "ingredients", "reviews"})
    Optional<Recipe> findById(Long id);
}
