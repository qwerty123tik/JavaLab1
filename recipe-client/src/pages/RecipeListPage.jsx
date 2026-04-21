import { useEffect, useState } from 'react';
import { Container, Row, Col, Spinner, Alert } from 'react-bootstrap';
import { searchRecipesNative, getReviewsByRecipe } from '../api/recipeApi';
import RecipeCard from '../components/RecipeCard';
import FilterBar from '../components/FilterBar';

export default function RecipeListPage() {
    const [recipes, setRecipes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ ingredient: '', category: '', title: '' });

    const loadRecipes = async () => {
        setLoading(true);
        try {
            const res = await searchRecipesNative(filters.ingredient, filters.category, filters.title, 0, 20, 'name,asc');
            const recipesData = res.data.content || [];

            const recipesWithRating = await Promise.all(recipesData.map(async (recipe) => {
                try {
                    const reviewsRes = await getReviewsByRecipe(recipe.id);
                    const reviews = reviewsRes.data || [];
                    const avgRating = reviews.length > 0
                        ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length
                        : 0;
                    return { ...recipe, averageRating: avgRating };
                } catch (err) {
                    return { ...recipe, averageRating: 0 };
                }
            }));

            setRecipes(recipesWithRating);
        } catch (err) {
            setError('Error loading recipes');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRecipes();
    }, [filters]);

    return (
        <>
            <div className="home-hero">
                <Container>
                    <h1>Recipes from around the world</h1>
                    <FilterBar filters={filters} setFilters={setFilters} onSearch={loadRecipes} />
                </Container>
            </div>
            <div className="bg-warm py-4">
                <Container>
                    {loading && <Spinner animation="border" className="d-block mx-auto" />}
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Row>
                        {recipes.map(recipe => (
                            <Col key={recipe.id} sm={12} md={6} lg={4} className="mb-4">
                                <RecipeCard recipe={recipe} />
                            </Col>
                        ))}
                    </Row>
                </Container>
            </div>
        </>
    );
}