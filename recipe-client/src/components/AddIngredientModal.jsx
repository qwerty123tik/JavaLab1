import { Modal, Button, Form } from 'react-bootstrap';
import { useState } from 'react';

export default function AddIngredientModal({ show, onClose, onAdd, units }) {
    const [name, setName] = useState('');
    const [unitAbbreviation, setUnitAbbreviation] = useState(units[0]?.abbreviation || '');

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!name.trim()) return;
        await onAdd({ name, unitAbbreviation });
        setName('');
        setUnitAbbreviation(units[0]?.abbreviation || '');
        onClose();
    };

    return (
        <Modal show={show} onHide={onClose}>
            <Modal.Header closeButton>
                <Modal.Title>Add new ingredient</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                        <Form.Label>Ingredient name</Form.Label>
                        <Form.Control
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Unit</Form.Label>
                        <Form.Select
                            value={unitAbbreviation}
                            onChange={(e) => setUnitAbbreviation(e.target.value)}
                        >
                            {units.map(unit => (
                                <option key={unit.id} value={unit.abbreviation}>
                                    {unit.name} ({unit.abbreviation})
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>
                    <Button variant="primary" type="submit">Create ingredient</Button>
                </Form>
            </Modal.Body>
        </Modal>
    );
}