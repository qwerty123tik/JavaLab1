package com.example.springrecipe.repository;

import com.example.springrecipe.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByName(String name);  // Поиск по названию
}
