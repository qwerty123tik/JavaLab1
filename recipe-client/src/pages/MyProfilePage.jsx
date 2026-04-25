import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRecipesByAuthor, getReviewsByUser, deleteRecipe } from '../api/recipeApi';
import { Container, Row, Col, ListGroup, Spinner, Alert } from 'react-bootstrap';
import RecipeCard from '../components/RecipeCard';

export default function MyProfilePage() {
    const { user } = useAuth();
    const [recipes, setRecipes] = useState([]);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!user) return;
        Promise.all([getRecipesByAuthor(user.id), getReviewsByUser(user.id)])
            .then(([recipesRes, reviewsRes]) => {
                setRecipes(recipesRes.data.content || recipesRes.data || []);
                setReviews(reviewsRes.data.content || reviewsRes.data || []);
                setLoading(false);
            })
            .catch(() => setError('Не удалось загрузить данные'));
    }, [user]);

    const handleDelete = async (id) => {
        if (window.confirm('Удалить рецепт? Это действие нельзя отменить.')) {
            try {
                await deleteRecipe(id);
                // Обновляем список рецептов после удаления
                const recipesRes = await getRecipesByAuthor(user.id);
                setRecipes(recipesRes.data.content || recipesRes.data || []);
            } catch (err) {
                alert('Ошибка при удалении рецепта');
            }
        }
    };

    if (loading) return <Container><Spinner animation="border" /></Container>;
    if (error) return <Container><Alert variant="danger">{error}</Alert></Container>;

    return (
        <Container className="mt-4">
            <h1>Мой профиль</h1>
            <h2>Мои рецепты</h2>
            {recipes.length === 0 ? (
                <p>Вы ещё не создали ни одного рецепта.</p>
            ) : (
                <Row>
                    {recipes.map(recipe => (
                        <Col key={recipe.id} sm={12} md={6} lg={4} className="mb-4 d-flex">
                            <RecipeCard recipe={recipe} onDelete={handleDelete} />
                        </Col>
                    ))}
                </Row>
            )}
            <h2>Мои отзывы</h2>
            {reviews.length === 0 ? (
                <p>Вы ещё не оставили ни одного отзыва.</p>
            ) : (
                <ListGroup>
                    {reviews.map(review => (
                        <ListGroup.Item key={review.id}>
                            <strong>Рецепт:</strong> <Link to={`/recipe/${review.recipeId}`}>{review.recipeName}</Link><br />
                            <strong>Оценка:</strong> {'⭐'.repeat(review.rating)}<br />
                            <strong>Комментарий:</strong> {review.comment}
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        </Container>
    );
}