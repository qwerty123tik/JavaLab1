package com.example.springrecipe.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class RecipeCacheKey {
    private String ingredientName;
    private int pageNumber;
    private int pageSize;
    private String sortBy;
    private String sortDirection;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecipeCacheKey that = (RecipeCacheKey) o;
        return Objects.equals(ingredientName, that.ingredientName) &&
                Objects.equals(pageNumber, that.pageNumber) &&
                Objects.equals(pageSize, that.pageSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientName, pageNumber, pageSize);
    }
}
