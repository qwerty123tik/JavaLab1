import { Modal, Button, Form } from 'react-bootstrap';
import { useState } from 'react';

export default function RegisterModal({ show, onClose, onRegister, userName }) {
    const [email, setEmail] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        onRegister(email);
    };

    return (
        <Modal show={show} onHide={onClose}>
            <Modal.Header closeButton>
                <Modal.Title>Create account</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <p>User <strong>{userName}</strong> not found. Would you like to create an account?</p>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Email</Form.Label>
                        <Form.Control
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </Form.Group>
                    <Button variant="primary" type="submit">Create and add review</Button>
                    <Button variant="secondary" onClick={onClose} className="ms-2">Cancel</Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
}