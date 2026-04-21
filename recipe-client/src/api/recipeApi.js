import apiClient from './client';

export const getRecipes = () => apiClient.get('/recipes');
export const getRecipeById = (id) => apiClient.get(`/recipes/${id}`);
export const createRecipe = (recipe) => apiClient.post('/recipes', recipe);
export const updateRecipe = (id, recipe) => apiClient.put(`/recipes/${id}`, recipe);
export const deleteRecipe = (id) => apiClient.delete(`/recipes/${id}`);
export const getRecipesByAuthor = (authorId) => apiClient.get(`/recipes/author/${authorId}`);

export const searchRecipesNative = (ingredient, category, title, page = 0, size = 10, sort = 'name,asc') =>
    apiClient.get('/recipes/search/native', { params: { ingredient, category, title, page, size, sort } });

export const getCategories = () => apiClient.get('/categories');
export const getIngredients = () => apiClient.get('/ingredients');
export const getUsers = () => apiClient.get('/users');
export const createUser = (userData) => apiClient.post('/users', userData);
export const updateUser = (id, userData) => apiClient.put(`/users/${id}`, userData);
export const deleteUser = (id) => apiClient.delete(`/users/${id}`);
export const getUnits = () => apiClient.get('/units');
export const createIngredient = (ingredientData) => apiClient.post('/ingredients', ingredientData);

export const getReviewsByRecipe = (recipeId) => apiClient.get(`/reviews/recipe/${recipeId}`);
export const createReview = (review) => apiClient.post('/reviews', review);
export const deleteReview = (id) => apiClient.delete(`/reviews/${id}`);
export const getReviewsByUser = (userId) => apiClient.get(`/reviews/user/${userId}`);