import { Form, Row, Col, Button } from 'react-bootstrap';

export default function FilterBar({ filters, setFilters, onSearch }) {
    const handleChange = (e) => {
        setFilters(prev => ({ ...prev, [e.target.name]: e.target.value }));
    };
    const handleSubmit = (e) => {
        e.preventDefault();
        onSearch();
    };
    return (
        <Form onSubmit={handleSubmit} className="filter-bar mb-4">
            <Row className="g-2 align-items-center">
                <Col md={3}>
                    <Form.Control
                        type="text"
                        name="ingredient"
                        placeholder="Search by ingredient..."
                        value={filters.ingredient || ''}
                        onChange={handleChange}
                    />
                </Col>
                <Col md={3}>
                    <Form.Control
                        type="text"
                        name="category"
                        placeholder="Search by category..."
                        value={filters.category || ''}
                        onChange={handleChange}
                    />
                </Col>
                <Col md={4}>
                    <Form.Control
                        type="text"
                        name="title"
                        placeholder="Search by title..."
                        value={filters.title || ''}
                        onChange={handleChange}
                    />
                </Col>
                <Col md={2}>
                    <Button type="submit" variant="primary" className="w-100">Search</Button>
                </Col>
            </Row>
        </Form>
    );
}