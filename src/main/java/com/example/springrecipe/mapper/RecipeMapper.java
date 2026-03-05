package com.example.springrecipe.mapper;

import com.example.springrecipe.dto.CategoryDTO;
import com.example.springrecipe.dto.IngredientDTO;
import com.example.springrecipe.dto.RecipeDTO;
import com.example.springrecipe.dto.ReviewDTO;
import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.dto.UserDTO;
import com.example.springrecipe.model.Category;
import com.example.springrecipe.model.Ingredient;
import com.example.springrecipe.model.Recipe;
import com.example.springrecipe.model.Review;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.model.User;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    public RecipeDTO toRecipeDTO(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setCookingTime(recipe.getCookingTime());

        if (recipe.getCategory() != null) {
            dto.setCategoryId(recipe.getCategory().getId());
            dto.setCategoryName(recipe.getCategory().getName());
        } else {
            dto.setCategoryId(null);
            dto.setCategoryName(null);
        }

        if (recipe.getAuthor() != null) {
            dto.setAuthorId(recipe.getAuthor().getId());
            dto.setAuthorName(recipe.getAuthor().getUserName());
        } else {
            dto.setAuthorId(null);
            dto.setAuthorName(null);
        }

        if (recipe.getIngredients() != null) {
            dto.setIngredientNames(recipe.getIngredients()
                    .stream()
                    .map(Ingredient::getName)
                    .toList());

            dto.setIngredients(recipe.getIngredients()
                    .stream()
                    .map(this::toIngredientDTO)
                    .toList());
        }
        dto.setAverageRating(0.0);
        /*if (recipe.getReviews() != null) {
            double avg = recipe.getReviews().stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            dto.setAverageRating(Math.round(avg * 10) / 10.0);
        } else {
            dto.setAverageRating(0.0);
        }*/

        return dto;
    }

    public IngredientDTO toIngredientDTO(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setName(ingredient.getName());

        if (ingredient.getUnit() != null) {
            dto.setUnitName(ingredient.getUnit().getName());
            dto.setUnitAbbreviation(ingredient.getUnit().getAbbreviation());
        }

        return dto;
    }

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    public CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        if (category.getRecipes() != null) {
            dto.setRecipeCount((long) category.getRecipes().size());
        } else {
            dto.setRecipeCount(0L);
        }

        return dto;
    }

    public ReviewDTO toReviewDTO(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getUserName());
        }

        if (review.getRecipe() != null) {
            dto.setRecipeId(review.getRecipe().getId());
            dto.setRecipeName(review.getRecipe().getName());
        }

        return dto;
    }

    public UnitDTO toUnitDto(UnitOfMeasure unit) {
        if (unit == null) {
            return null;
        }

        UnitDTO dto = new UnitDTO();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        dto.setAbbreviation(unit.getAbbreviation());
        return dto;
    }
}
