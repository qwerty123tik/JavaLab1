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
                    Recipe System
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="ms-auto">
                        <Nav.Link as={Link} to="/">Recipes</Nav.Link>
                        <Nav.Link as={Link} to="/users">Users</Nav.Link>
                        {user && <Nav.Link as={Link} to="/my-profile">My Profile</Nav.Link>}
                        {user && <Nav.Link as={Link} to="/create">Add Recipe</Nav.Link>}
                        {user ? (
                            <>
                                <Navbar.Text className="me-2">Welcome, {user.userName}</Navbar.Text>
                                <Button variant="outline-danger" size="sm" onClick={handleLogout}>Logout</Button>
                            </>
                        ) : (
                            <>
                                <Nav.Link as={Link} to="/login">Login</Nav.Link>
                                <Nav.Link as={Link} to="/register">Register</Nav.Link>
                            </>
                        )}
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}