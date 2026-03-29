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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeMapper mapper;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewDTO dto() {
        ReviewDTO dto = new ReviewDTO();
        dto.setRating(5);
        dto.setComment("Отлично!");
        dto.setUserId(1L);
        dto.setRecipeId(1L);
        return dto;
    }

    @Test
    void getAllReviews_success() {
        Review review = new Review();
        review.setRating(5);

        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(mapper.toReviewDTO(any())).thenReturn(dto());

        List<ReviewDTO> result = reviewService.getAllReviews();

        assertEquals(1, result.size());
        verify(reviewRepository).findAll();
    }

    @Test
    void getReviewById_success() {
        Review review = new Review();
        review.setId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(mapper.toReviewDTO(any())).thenReturn(dto());

        ReviewDTO result = reviewService.getReviewById(1L);

        assertEquals(5, result.getRating());
    }

    @Test
    void getReviewById_notFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(1L));
    }

    @Test
    void getReviewsByRecipe_success() {
        when(reviewRepository.findByRecipeId(1L)).thenReturn(List.of(new Review()));
        when(mapper.toReviewDTO(any())).thenReturn(dto());

        List<ReviewDTO> result = reviewService.getReviewsByRecipe(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getReviewsByUser_success() {
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(new Review()));
        when(mapper.toReviewDTO(any())).thenReturn(dto());

        List<ReviewDTO> result = reviewService.getReviewsByUser(1L);

        assertEquals(1, result.size());
    }

    @Test
    void createReview_success() {
        ReviewDTO dto = dto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(new Recipe()));
        when(reviewRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toReviewDTO(any())).thenReturn(dto);

        ReviewDTO result = reviewService.createReview(dto);

        assertEquals(5, result.getRating());
    }

    @Test
    void createReview_userNotFound() {
        ReviewDTO dto = dto();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> reviewService.createReview(dto));
    }

    @Test
    void createReview_recipeNotFound() {
        ReviewDTO dto = dto();
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecipeNotFoundException.class, () -> reviewService.createReview(dto));
    }

    @Test
    void updateReview_success() {
        Review review = new Review();
        review.setId(1L);
        review.setRating(3);

        ReviewDTO dto = dto();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toReviewDTO(any())).thenReturn(dto);

        ReviewDTO result = reviewService.updateReview(1L, dto);

        assertEquals(5, result.getRating());
    }

    @Test
    void updateReview_notFound() {
        ReviewDTO dto = dto();
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewService.updateReview(1L, dto));
    }

    @Test
    void deleteReview_success() {
        when(reviewRepository.existsById(1L)).thenReturn(true);

        reviewService.deleteReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_notFound() {
        when(reviewRepository.existsById(1L)).thenReturn(false);

        assertThrows(ReviewNotFoundException.class, () -> reviewService.deleteReview(1L));
    }
}
