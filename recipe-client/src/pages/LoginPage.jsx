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
                setError('Пользователь не найден');
                return;
            }
            let stored = localStorage.getItem('recipeUser');
            let storedPassword = stored ? JSON.parse(stored).password : null;
            if (!storedPassword) {
                const newPassword = prompt('Пароль не найден. Введите новый пароль:');
                if (newPassword) {
                    login({ ...user, password: newPassword });
                    navigate('/');
                } else {
                    setError('Пароль не задан');
                }
                return;
            }
            if (storedPassword === password) {
                login({ ...user, password });
                navigate('/');
            } else {
                setError('Неверный пароль');
            }
        } catch (err) {
            setError('Ошибка входа');
        }
    };

    return (
        <Container className="mt-5" style={{ maxWidth: '400px' }}>
            <h2 className="text-center mb-4">Вход</h2>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
                <Form.Group className="mb-3">
                    <Form.Label>Имя пользователя</Form.Label>
                    <Form.Control value={userName} onChange={e => setUserName(e.target.value)} required />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Пароль</Form.Label>
                    <Form.Control type="password" value={password} onChange={e => setPassword(e.target.value)} required />
                </Form.Group>
                <Button type="submit" variant="primary" className="w-100">Войти</Button>
            </Form>
            <div className="text-center mt-3">
                Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
            </div>
        </Container>
    );
}