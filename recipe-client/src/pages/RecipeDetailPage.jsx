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
    const { user } = useAuth();
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
                setError('Не удалось загрузить рецепт или отзывы');
                setLoading(false);
            });
    }, [id]);

    const loadReviews = async () => {
        try {
            const res = await getReviewsByRecipe(id);
            setReviews(res.data);
        } catch (err) {
            console.error('Ошибка загрузки отзывов', err);
        }
    };

    const handleReviewSubmit = async (e) => {
        e.preventDefault();
        if (!user) {
            setError('Вы должны войти, чтобы оставить отзыв');
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
            setError('Не удалось добавить отзыв');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDeleteReview = async (reviewId) => {
        if (window.confirm('Удалить этот отзыв?')) {
            try {
                await deleteReview(reviewId);
                await loadReviews();
            } catch (err) {
                setError('Не удалось удалить отзыв');
            }
        }
    };

    const handleDeleteRecipe = async () => {
        if (window.confirm('Удалить рецепт? Все связанные отзывы также будут удалены.')) {
            try {
                await deleteRecipe(id);
                navigate('/');
            } catch (err) {
                setError('Не удалось удалить рецепт');
            }
        }
    };

    const handleEditRecipe = () => {
        navigate(`/edit/${id}`);
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!recipe) return <Container className="mt-4">Рецепт не найден</Container>;

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
                        <p><strong>Автор:</strong> {recipe.authorName}</p>
                        <p><strong>Категория:</strong> {recipe.categoryName}</p>
                        <p><strong>Время:</strong> {recipe.cookingTime} мин</p>
                    </div>
                    <p>{recipe.description}</p>
                    {isAuthor && (
                        <div className="mt-3">
                            <Button variant="secondary" onClick={handleEditRecipe} className="me-2">Редактировать рецепт</Button>
                            <Button variant="danger" onClick={handleDeleteRecipe}>Удалить рецепт</Button>
                        </div>
                    )}
                </Col>
            </Row>

            <h3 className="mt-4">Ингредиенты</h3>
            <ListGroup className="mb-4">
                {recipe.recipeIngredients?.map(ing => (
                    <ListGroup.Item key={ing.ingredientId}>
                        {ing.ingredientName} – {ing.quantity} {ing.unitAbbreviation}
                    </ListGroup.Item>
                ))}
            </ListGroup>

            <h3>Отзывы</h3>
            {user ? (
                <Form onSubmit={handleReviewSubmit} className="mb-4 p-3 bg-light rounded">
                    <Form.Group className="mb-2">
                        <Form.Label>Оценка</Form.Label>
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
                        <Form.Label>Комментарий</Form.Label>
                        <Form.Control
                            as="textarea"
                            rows={2}
                            value={newReview.comment}
                            onChange={e => setNewReview({ ...newReview, comment: e.target.value })}
                        />
                    </Form.Group>
                    <Button variant="primary" type="submit" disabled={submitting}>
                        {submitting ? 'Добавление...' : 'Добавить отзыв'}
                    </Button>
                </Form>
            ) : (
                <Alert variant="info">
                    Пожалуйста, <Link to="/login">войдите</Link>, чтобы оставить отзыв.
                </Alert>
            )}

            {reviews.length === 0 ? (
                <Alert variant="info">Пока нет отзывов. Будьте первым!</Alert>
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
                                    Удалить
                                </Button>
                            )}
                        </ListGroup.Item>
                    ))}
                </ListGroup>
            )}
        </Container>
    );
}