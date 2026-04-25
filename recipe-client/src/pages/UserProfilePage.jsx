import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getUsers, getRecipesByAuthor, getReviewsByUser, updateUser, deleteUser } from '../api/recipeApi';
import { Container, Row, Col, Image, ListGroup, Tab, Tabs, Spinner, Alert, Button, Form, Modal, OverlayTrigger, Tooltip } from 'react-bootstrap';
import { FaEdit, FaTrashAlt } from 'react-icons/fa';
import { useAuth } from '../context/AuthContext';

export default function UserProfilePage() {
    const { userId } = useParams();
    const navigate = useNavigate();
    const { user: currentUser } = useAuth();
    const [user, setUser] = useState(null);
    const [recipes, setRecipes] = useState([]);
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showEditModal, setShowEditModal] = useState(false);
    const [editForm, setEditForm] = useState({ userName: '', email: '', avatarUrl: '' });

    useEffect(() => {
        const fetchData = async () => {
            try {
                const usersRes = await getUsers();
                const foundUser = usersRes.data.find(u => u.id == userId);
                if (!foundUser) throw new Error('User not found');
                setUser(foundUser);
                setEditForm({
                    userName: foundUser.userName,
                    email: foundUser.email,
                    avatarUrl: foundUser.avatarUrl || ''
                });

                const recipesRes = await getRecipesByAuthor(userId);
                setRecipes(recipesRes.data.content || recipesRes.data || []);

                const reviewsRes = await getReviewsByUser(userId);
                setReviews(reviewsRes.data.content || reviewsRes.data || []);
            } catch (err) {
                setError('Не удалось загрузить данные пользователя');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [userId]);

    const handleUpdateUser = async () => {
        try {
            const updated = await updateUser(userId, editForm);
            setUser(updated.data);
            setShowEditModal(false);
        } catch (err) {
            setError('Не удалось обновить пользователя');
        }
    };

    const handleDeleteUser = async () => {
        if (window.confirm('Удалить этого пользователя? Все его рецепты и отзывы будут удалены.')) {
            try {
                await deleteUser(userId);
                navigate('/users');
            } catch (err) {
                setError('Не удалось удалить пользователя');
            }
        }
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!user) return <Container className="mt-4">Пользователь не найден</Container>;

    return (
        <Container className="mt-4">
            <Row>
                <Col md={3} className="text-center">
                    <Image
                        src={user.avatarUrl || 'https://via.placeholder.com/150'}
                        roundedCircle
                        fluid
                        style={{ width: '150px', height: '150px', objectFit: 'cover' }}
                    />
                    <h3 className="mt-3">{user.userName}</h3>
                    <p>{user.email}</p>
                    <div className="d-flex justify-content-center gap-2 mt-3">
                        <OverlayTrigger placement="top" overlay={<Tooltip>Редактировать профиль</Tooltip>}>
                            <Button variant="outline-secondary" size="sm" onClick={() => setShowEditModal(true)}>
                                <FaEdit />
                            </Button>
                        </OverlayTrigger>
                        <OverlayTrigger placement="top" overlay={<Tooltip>Удалить аккаунт</Tooltip>}>
                            <Button variant="outline-danger" size="sm" onClick={handleDeleteUser}>
                                <FaTrashAlt />
                            </Button>
                        </OverlayTrigger>
                    </div>
                </Col>
                <Col md={9}>
                    <Tabs defaultActiveKey="recipes" className="mb-3">
                        <Tab eventKey="recipes" title={`Рецепты (${recipes.length})`}>
                            {recipes.length === 0 ? (
                                <p>Нет рецептов.</p>
                            ) : (
                                <Row>
                                    {recipes.map(recipe => (
                                        <Col md={4} key={recipe.id} className="mb-3">
                                            <Link to={`/recipe/${recipe.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                                                <div className="border rounded p-2 text-center h-100">
                                                    <img
                                                        src={recipe.imageUrl || 'https://picsum.photos/100/100'}
                                                        style={{ width: '100%', height: '100px', objectFit: 'cover' }}
                                                        alt={recipe.name}
                                                    />
                                                    <div className="mt-2">{recipe.name}</div>
                                                </div>
                                            </Link>
                                        </Col>
                                    ))}
                                </Row>
                            )}
                        </Tab>
                        <Tab eventKey="reviews" title={`Отзывы (${reviews.length})`}>
                            {reviews.length === 0 ? (
                                <p>Нет отзывов.</p>
                            ) : (
                                <ListGroup>
                                    {reviews.map(review => (
                                        <ListGroup.Item key={review.id}>
                                            <strong>Рецепт:</strong>{' '}
                                            <Link to={`/recipe/${review.recipeId}`}>{review.recipeName}</Link>
                                            <br />
                                            <strong>Оценка:</strong> {'⭐'.repeat(review.rating)}
                                            <br />
                                            <strong>Комментарий:</strong> {review.comment}
                                        </ListGroup.Item>
                                    ))}
                                </ListGroup>
                            )}
                        </Tab>
                    </Tabs>
                </Col>
            </Row>

            <Modal show={showEditModal} onHide={() => setShowEditModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Редактировать профиль</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Имя пользователя</Form.Label>
                            <Form.Control
                                type="text"
                                value={editForm.userName}
                                onChange={e => setEditForm({ ...editForm, userName: e.target.value })}
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Email</Form.Label>
                            <Form.Control
                                type="email"
                                value={editForm.email}
                                onChange={e => setEditForm({ ...editForm, email: e.target.value })}
                            />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>URL аватара</Form.Label>
                            <Form.Control
                                type="text"
                                value={editForm.avatarUrl}
                                onChange={e => setEditForm({ ...editForm, avatarUrl: e.target.value })}
                                placeholder="https://example.com/avatar.jpg"
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer className="d-flex justify-content-between gap-2">
                    <Button variant="secondary" onClick={() => setShowEditModal(false)} className="rounded-pill flex-fill">
                        Отмена
                    </Button>
                    <Button variant="primary" onClick={handleUpdateUser} className="rounded-pill flex-fill">
                        Сохранить
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}