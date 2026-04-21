import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Container, Alert } from 'react-bootstrap';
import { getUsers } from '../api/recipeApi';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const [userName, setUserName] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

   const handleSubmit = async (e) => {
       e.preventDefault();
       try {
           const usersRes = await getUsers();
           const user = usersRes.data.find(u => u.userName === userName);
           if (!user) {
               setError('User not found');
               return;
           }
           let stored = localStorage.getItem('recipeUser');
           let storedPassword = stored ? JSON.parse(stored).password : null;
           if (!storedPassword) {
               const newPassword = prompt('No password found for this user. Enter a new password:');
               if (newPassword) {
                   login({ ...user, password: newPassword });
                   navigate('/');
               } else {
                   setError('Password not set');
               }
               return;
           }
           if (storedPassword === password) {
               login({ ...user, password });
               navigate('/');
           } else {
               setError('Invalid password');
           }
       } catch (err) {
           setError('Login failed');
       }
   };

    return (
        <Container className="mt-5" style={{ maxWidth: '400px' }}>
            <h2 className="text-center mb-4">Login</h2>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                    <Form.Label>Username</Form.Label>
                    <Form.Control value={userName} onChange={e => setUserName(e.target.value)} required />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Password</Form.Label>
                    <Form.Control type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                </Form.Group>
                <Button type="submit" variant="primary" className="w-100">Login</Button>
            </Form>
            <div className="text-center mt-3">
                No account? <Link to="/register">Register</Link>
            </div>
        </Container>
    );
}