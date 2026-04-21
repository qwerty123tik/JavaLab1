import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getUsers, getRecipesByAuthor, getReviewsByUser, updateUser, deleteUser } from '../api/recipeApi';
import { Container, Row, Col, Image, ListGroup, Tab, Tabs, Spinner, Alert, Button, Form, Modal } from 'react-bootstrap';

export default function UserProfilePage() {
    const { userId } = useParams();
    const navigate = useNavigate();
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
                setError('Failed to load user data');
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
            setError('Failed to update user');
        }
    };

    const handleDeleteUser = async () => {
        if (window.confirm('Delete this user? All their recipes and reviews will be deleted.')) {
            try {
                await deleteUser(userId);
                navigate('/users');
            } catch (err) {
                setError('Failed to delete user');
            }
        }
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!user) return <Container className="mt-4">User not found</Container>;

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
                    <div>
                        <Button variant="secondary" size="sm" onClick={() => setShowEditModal(true)} className="me-2">Edit Profile</Button>
                        <Button variant="danger" size="sm" onClick={handleDeleteUser}>Delete Account</Button>
                    </div>
                </Col>
                <Col md={9}>
                    <Tabs defaultActiveKey="recipes" className="mb-3">
                        <Tab eventKey="recipes" title={`Recipes (${recipes.length})`}>
                            {recipes.length === 0 ? (
                                <p>No recipes yet.</p>
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
                        <Tab eventKey="reviews" title={`Reviews (${reviews.length})`}>
                            {reviews.length === 0 ? (
                                <p>No reviews yet.</p>
                            ) : (
                                <ListGroup>
                                    {reviews.map(review => (
                                        <ListGroup.Item key={review.id}>
                                            <strong>Recipe:</strong>{' '}
                                            <Link to={`/recipe/${review.recipeId}`}>{review.recipeName}</Link>
                                            <br />
                                            <strong>Rating:</strong> {'⭐'.repeat(review.rating)}
                                            <br />
                                            <strong>Comment:</strong> {review.comment}
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
                    <Modal.Title>Edit Profile</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Username</Form.Label>
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
                            <Form.Label>Avatar URL</Form.Label>
                            <Form.Control
                                type="text"
                                value={editForm.avatarUrl}
                                onChange={e => setEditForm({ ...editForm, avatarUrl: e.target.value })}
                                placeholder="https://example.com/avatar.jpg"
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowEditModal(false)}>Cancel</Button>
                    <Button variant="primary" onClick={handleUpdateUser}>Save</Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}