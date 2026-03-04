package com.example.springrecipe.repository;

import com.example.springrecipe.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByRecipeId(Long recipeId);

    List<Review> findByUserId(Long userId);

}
