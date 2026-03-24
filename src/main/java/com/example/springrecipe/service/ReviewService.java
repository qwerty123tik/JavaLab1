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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeMapper mapper;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        log.debug("Запрос всех отзывов");
        List<ReviewDTO> result = reviewRepository.findAll().stream()
                .map(mapper::toReviewDTO)
                .toList();
        log.info("Найдено {} отзывов", result.size());
        return result;
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(Long id) {
        log.debug("Поиск отзыва по ID: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Отзыв с ID {} не найден", id);
                    return new ReviewNotFoundException("Review not found");
                });
        log.info("Найден отзыв ID={} с рейтингом {}", id, review.getRating());
        return mapper.toReviewDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByRecipe(Long recipeId) {
        log.debug("Поиск отзывов для рецепта ID: {}", recipeId);
        List<ReviewDTO> result = reviewRepository.findByRecipeId(recipeId).stream()
                .map(mapper::toReviewDTO)
                .toList();
        log.info("Найдено {} отзывов для рецепта ID {}", result.size(), recipeId);
        return result;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUser(Long userId) {
        log.debug("Поиск отзывов пользователя ID: {}", userId);
        List<ReviewDTO> result = reviewRepository.findByUserId(userId).stream()
                .map(mapper::toReviewDTO)
                .toList();
        log.info("Найдено {} отзывов пользователя ID {}", result.size(), userId);
        return result;
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO dto) {
        log.info("Создание нового отзыва: рейтинг={}, рецептId={}, пользовательId={}",
                dto.getRating(), dto.getRecipeId(), dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", dto.getUserId());
                    return new UserNotFoundException("User not found");
                });

        Recipe recipe = recipeRepository.findById(dto.getRecipeId())
                .orElseThrow(() -> {
                    log.error("Рецепт с ID {} не найден", dto.getRecipeId());
                    return new RecipeNotFoundException("Recipe not found");
                });

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setUser(user);
        review.setRecipe(recipe);

        review = reviewRepository.save(review);
        log.info("Отзыв успешно создан: ID={}, рейтинг={}", review.getId(), review.getRating());
        return mapper.toReviewDTO(review);
    }

    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO dto) {
        log.info("Обновление отзыва: ID={}, новый рейтинг={}", id, dto.getRating());

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Отзыв с ID {} не найден для обновления", id);
                    return new ReviewNotFoundException("Review not found");
                });

        int oldRating = review.getRating();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        log.debug("Рейтинг изменен: {} -> {}", oldRating, dto.getRating());
        review = reviewRepository.save(review);
        log.info("Отзыв ID={} успешно обновлен", id);

        return mapper.toReviewDTO(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        log.warn("УДАЛЕНИЕ ОТЗЫВА: ID={}", id);

        if (!reviewRepository.existsById(id)) {
            log.warn("Отзыв с ID {} не найден для удаления", id);
            throw new ReviewNotFoundException("Review not found");
        }

        reviewRepository.deleteById(id);
        log.info("Отзыв ID={} успешно удален", id);
    }
}
