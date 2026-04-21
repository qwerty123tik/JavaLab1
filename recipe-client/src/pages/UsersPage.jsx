import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getUsers } from '../api/recipeApi';
import { Container, Row, Col, Card, Image, Spinner } from 'react-bootstrap';

export default function UsersPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        getUsers()
            .then(res => setUsers(res.data))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;

    return (
        <Container className="mt-4">
            <h1>Users</h1>
            <Row>
                {users.map(user => (
                    <Col md={4} key={user.id} className="mb-4">
                        <Link to={`/user/${user.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                            <Card className="h-100 text-center">
                                <Card.Body>
                                    <Image
                                        src={user.avatarUrl || 'https://via.placeholder.com/100'}
                                        roundedCircle
                                        style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                                    />
                                    <Card.Title className="mt-3">{user.userName}</Card.Title>
                                    <Card.Text>{user.email}</Card.Text>
                                </Card.Body>
                            </Card>
                        </Link>
                    </Col>
                ))}
            </Row>
        </Container>
    );
}