import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function NavBar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <Navbar expand="lg" className="py-3">
            <Container>
                <Navbar.Brand as={Link} to="/" className="fw-bold">
                    Система рецептов
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="ms-auto">
                        <Nav.Link as={Link} to="/">Рецепты</Nav.Link>
                        <Nav.Link as={Link} to="/users">Пользователи</Nav.Link>
                        {user && <Nav.Link as={Link} to="/my-profile">Мой профиль</Nav.Link>}
                        {user ? (
                            <>
                                <Navbar.Text className="me-2">Привет, {user.userName}</Navbar.Text>
                                <Button variant="outline-danger" size="sm" onClick={handleLogout}>Выйти</Button>
                            </>
                        ) : (
                            <>
                                <Nav.Link as={Link} to="/login">Войти</Nav.Link>
                                <Nav.Link as={Link} to="/register">Регистрация</Nav.Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}