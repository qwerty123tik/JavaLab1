import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRecipesByAuthor, getReviewsByUser } from '../api/recipeApi';
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
            .catch(() => setError('Failed to load data'));
    }, [user]);

    if (loading) return <Container><Spinner animation="border" /></Container>;
    if (error) return <Container><Alert variant="danger">{error}</Alert></Container>;

    return (
        <Container className="mt-4">
            <h1>My Profile</h1>
            <h2>My Recipes</h2>
            {recipes.length === 0 ? (
                <p>You haven't created any recipes yet.</p>
            ) : (
                <Row>
                    {recipes.map(recipe => (
                        <Col key={recipe.id} sm={12} md={6} lg={4} className="mb-4">
                            <Link to={`/recipe/${recipe.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                <RecipeCard recipe={recipe} />
                            </Link>
                        </Col>
                    ))}
                </Row>
            )}
            <h2>My Reviews</h2>
            {reviews.length === 0 ? (
                <p>You haven't written any reviews yet.</p>
            ) : (
                <ListGroup>
                    {reviews.map(review => (
                        <ListGroup.Item key={review.id}>
                            <strong>Recipe:</strong> <Link to={`/recipe/${review.recipeId}`}>{review.recipeName}</Link><br />
                            <strong>Rating:</strong> {'⭐'.repeat(review.rating)}<br />
                            <strong>Comment:</strong> {review.comment}
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        </Container>
    );
}