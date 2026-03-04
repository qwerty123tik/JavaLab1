package com.example.springrecipe.service;

import com.example.springrecipe.dto.ReviewDTO;
import com.example.springrecipe.exceptions.RecipeNotFoundException;
import com.example.springrecipe.exceptions.ReviewNotFoundException;
import com.example.springrecipe.exceptions.UserNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.Review;
import com.example.springrecipe.model.User;
import com.example.springrecipe.repository.RecipeRepository;
import com.example.springrecipe.repository.ReviewRepository;
import com.example.springrecipe.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(mapper::toReviewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        return mapper.toReviewDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByRecipe(Long recipeId) {
        return reviewRepository.findByRecipeId(recipeId).stream()
                .map(mapper::toReviewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(mapper::toReviewDTO)
                .toList();
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Recipe recipe = recipeRepository.findById(dto.getRecipeId())
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setUser(user);
        review.setRecipe(recipe);

        review = reviewRepository.save(review);
        return mapper.toReviewDTO(review);
    }

    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        review = reviewRepository.save(review);
        return mapper.toReviewDTO(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException("Review not found");
        }
        reviewRepository.deleteById(id);
    }
}
