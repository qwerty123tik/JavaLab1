package com.example.springrecipe.controller;

import com.example.springrecipe.dto.ReviewDTO;
import com.example.springrecipe.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@AllArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByRecipe(@PathVariable Long recipeId) {
        return ResponseEntity.ok(reviewService.getReviewsByRecipe(recipeId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewDTO dto) {
        return new ResponseEntity<>(reviewService.createReview(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewDTO dto) {
        return ResponseEntity.ok(reviewService.updateReview(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
