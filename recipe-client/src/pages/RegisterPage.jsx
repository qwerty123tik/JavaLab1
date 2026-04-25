import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Form, Button, Container, Alert } from 'react-bootstrap';
import { createUser } from '../api/recipeApi';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
    const [userName, setUserName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirm, setConfirm] = useState('');
    const [error, setError] = useState('');
    const { register, login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (password !== confirm) {
            setError('Пароли не совпадают');
            return;
        }
        try {
            const res = await createUser({ userName, email });
            const newUser = res.data;
            register({ ...newUser, password });
            login({ ...newUser, password });
            navigate('/');
        } catch (err) {
            setError('Регистрация не удалась. Возможно, имя или email уже используются.');
        }
    };

    return (
        <Container className="mt-5" style={{ maxWidth: '400px' }}>
            <h2 className="text-center mb-4">Регистрация</h2>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                    <Form.Label>Имя пользователя</Form.Label>
                    <Form.Control value={userName} onChange={e => setUserName(e.target.value)} required />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" value={email} onChange={e => setEmail(e.target.value)} required />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Пароль</Form.Label>
                    <Form.Control type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Подтверждение пароля</Form.Label>
                    <Form.Control type="password" value={confirm} onChange={e => setConfirm(e.target.value)} required />
                </Form.Group>
                <Button type="submit" variant="primary" className="w-100">Зарегистрироваться</Button>
            </Form>
            <div className="text-center mt-3">
                Уже есть аккаунт? <Link to="/login">Войти</Link>
            </div>
        </Container>
    );
}