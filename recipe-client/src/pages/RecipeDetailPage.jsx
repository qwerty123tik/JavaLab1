import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
    getRecipeById,
    createReview,
    deleteReview,
    getReviewsByRecipe,
    deleteRecipe
} from '../api/recipeApi';
import { Container, Row, Col, ListGroup, Button, Form, Alert, Spinner } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';

export default function RecipeDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth(); // текущий авторизованный пользователь
    const [recipe, setRecipe] = useState(null);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [newReview, setNewReview] = useState({ rating: 5, comment: '' });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        Promise.all([getRecipeById(id), getReviewsByRecipe(id)])
            .then(([recipeRes, reviewsRes]) => {
                setRecipe(recipeRes.data);
                setReviews(reviewsRes.data);
                setLoading(false);
            })
            .catch(err => {
                setError('Failed to load recipe or reviews');
                setLoading(false);
            });
    }, [id]);

    const loadReviews = async () => {
        try {
            const res = await getReviewsByRecipe(id);
            setReviews(res.data);
        } catch (err) {
            console.error('Error loading reviews', err);
        }
    };

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (!user) {
            setError('You must be logged in to add a review');
            return;
        }
        setSubmitting(true);
        try {
            await createReview({
                rating: newReview.rating,
                comment: newReview.comment,
                userId: user.id,
                recipeId: id
            });
            setNewReview({ rating: 5, comment: '' });
            await loadReviews();
        } catch (err) {
            setError('Failed to add review');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (window.confirm('Delete this review?')) {
            try {
                await deleteReview(reviewId);
                await loadReviews();
            } catch (err) {
                setError('Failed to delete review');
            }
        }
    };

    const handleDeleteRecipe = async () => {
        if (window.confirm('Delete this recipe? This will also delete all its reviews.')) {
            try {
                await deleteRecipe(id);
                navigate('/');
            } catch (err) {
                setError('Failed to delete recipe');
            }
        }
    };

    const handleEditRecipe = () => {
        navigate(`/edit/${id}`);
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!recipe) return <Container className="mt-4">Recipe not found</Container>;

    const isAuthor = user && user.id === recipe.authorId;

    return (
        <Container className="mt-4">
            <Row>
                <Col md={6}>
                    <img
                        src={recipe.imageUrl || 'https://picsum.photos/600/400'}
                        alt={recipe.name}
                        className="recipe-detail-img"
                    />
                </Col>
                <Col md={6}>
                    <h1>{recipe.name}</h1>
                    <div className="recipe-meta">
                        <p><strong>Author:</strong> {recipe.authorName}</p>
                        <p><strong>Category:</strong> {recipe.categoryName}</p>
                        <p><strong>Time:</strong> {recipe.cookingTime} min</p>
                    </div>
                    <p>{recipe.description}</p>
                    {isAuthor && (
                        <div className="mt-3">
                            <Button variant="secondary" onClick={handleEditRecipe} className="me-2">Edit Recipe</Button>
                            <Button variant="danger" onClick={handleDeleteRecipe}>Delete Recipe</Button>
                        </div>
                    )}
                </Col>
            </Row>

            <h3 className="mt-4">Ingredients</h3>
            <ListGroup className="mb-4">
                {recipe.recipeIngredients?.map(ing => (
                    <ListGroup.Item key={ing.ingredientId}>
                        {ing.ingredientName} – {ing.quantity} {ing.unitAbbreviation}
                    </ListGroup.Item>
                ))}
            </ListGroup>

            <h3>Reviews</h3>
            {user ? (
                <Form onSubmit={handleReviewSubmit} className="mb-4 p-3 bg-light rounded">
                    <Form.Group className="mb-2">
                        <Form.Label>Rating</Form.Label>
                        <Form.Select
                            value={newReview.rating}
                            onChange={e => setNewReview({ ...newReview, rating: parseInt(e.target.value) })}
                        >
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                        </Form.Select>
                    </Form.Group>
                    <Form.Group className="mb-2">
                        <Form.Label>Comment</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={2}
                            value={newReview.comment}
                            onChange={e => setNewReview({ ...newReview, comment: e.target.value })}
                        />
                    </Form.Group>
                    <Button variant="primary" type="submit" disabled={submitting}>
                        {submitting ? 'Adding...' : 'Add review'}
                    </Button>
                </Form>
            ) : (
                <Alert variant="info">
                    Please <Link to="/login">login</Link> to leave a review.
                </Alert>
            )}

            {reviews.length === 0 ? (
                <Alert variant="info">No reviews yet. Be the first!</Alert>
            ) : (
                <ListGroup>
                    {reviews.map(review => (
                        <ListGroup.Item key={review.id} className="review-card">
                            <div className="rating mb-1">
                                {'⭐'.repeat(review.rating)}
                                {'☆'.repeat(5 - review.rating)}
                            </div>
                            <div className="comment">{review.comment}</div>
                            <div className="user-name mt-2">— {review.userName}</div>
                            {(user && user.id === review.userId) && (
                                <Button
                                    variant="outline-danger"
                                    size="sm"
                                    className="mt-2"
                                    onClick={() => handleDeleteReview(review.id)}
                                >
                                    Delete
                                </Button>
                            )}
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        </Container>
    );
}