import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import { getRecipeById, updateRecipe, getCategories, getIngredients, getUnits, createIngredient } from '../api/recipeApi';
import { useAuth } from '../context/AuthContext';

export default function RecipeEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [categories, setCategories] = useState([]);
    const [existingIngredients, setExistingIngredients] = useState([]);
    const [units, setUnits] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        cookingTime: '',
        categoryId: '',
        imageUrl: ''
    });
    const [selectedIngredients, setSelectedIngredients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showNewIngredient, setShowNewIngredient] = useState(false);
    const [newIngredient, setNewIngredient] = useState({
        name: '',
        unitAbbreviation: '',
        quantity: 1
    });
    const [isAuthor, setIsAuthor] = useState(false);

    useEffect(() => {
        Promise.all([getRecipeById(id), getCategories(), getIngredients(), getUnits()])
            .then(([recipeRes, catRes, ingRes, unitRes]) => {
                const recipe = recipeRes.data;
                // Проверяем, что текущий пользователь – автор рецепта
                if (!user || user.id !== recipe.authorId) {
                    setError('You are not the author of this recipe');
                    setLoading(false);
                    return;
                }
                setIsAuthor(true);
                setFormData({
                    name: recipe.name,
                    description: recipe.description || '',
                    cookingTime: recipe.cookingTime,
                    categoryId: recipe.categoryId,
                    imageUrl: recipe.imageUrl || ''
                });
                const selected = recipe.recipeIngredients.map(ing => ({
                    ingredientId: ing.ingredientId,
                    ingredientName: ing.ingredientName,
                    quantity: ing.quantity,
                    unitAbbreviation: ing.unitAbbreviation,
                    isNew: false
                }));
                setSelectedIngredients(selected);
                setCategories(catRes.data);
                setExistingIngredients(ingRes.data);
                setUnits(unitRes.data);
                setLoading(false);
            })
            .catch(() => setError('Failed to load recipe'));
    }, [id, user]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleExistingIngredientToggle = (ingredient) => {
        const existing = selectedIngredients.find(i => i.ingredientId === ingredient.id && !i.isNew);
        if (existing) {
            setSelectedIngredients(selectedIngredients.filter(i => !(i.ingredientId === ingredient.id && !i.isNew)));
        } else {
            const defaultUnit = units.length > 0 ? units[0].abbreviation : '';
            setSelectedIngredients([
                ...selectedIngredients,
                {
                    ingredientId: ingredient.id,
                    ingredientName: ingredient.name,
                    quantity: 1,
                    unitAbbreviation: defaultUnit,
                    isNew: false
                }
            ]);
        }
    };

    const handleIngredientChange = (ingredientId, field, value) => {
        setSelectedIngredients(prev =>
            prev.map(ing =>
                ing.ingredientId === ingredientId ? { ...ing, [field]: value } : ing
            )
        );
    };

    const handleAddNewIngredient = () => {
        if (!newIngredient.name.trim()) {
            setError('Please enter ingredient name');
            return;
        }

        const duplicate = existingIngredients.some(ing => ing.name.toLowerCase() === newIngredient.name.trim().toLowerCase());
        if (duplicate) {
            setError(`Ingredient "${newIngredient.name}" already exists. Please select it from the list.`);
            return;
        }
        const tempId = Date.now();
        setSelectedIngredients([
            ...selectedIngredients,
            {
                ingredientId: tempId,
                ingredientName: newIngredient.name,
                quantity: newIngredient.quantity,
                unitAbbreviation: newIngredient.unitAbbreviation,
                isNew: true,
                tempId: tempId
            }
        ]);
        setNewIngredient({ name: '', unitAbbreviation: '', quantity: 1 });
        setShowNewIngredient(false);
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (selectedIngredients.length === 0) {
            setError('Select at least one ingredient');
            return;
        }
        const invalid = selectedIngredients.some(ing => !ing.quantity || ing.quantity <= 0);
        if (invalid) {
            setError('Please enter a valid quantity (>0) for each ingredient');
            return;
        }

        const recipeToSend = {
            ...formData,
            cookingTime: parseInt(formData.cookingTime),
            authorId: user.id,
            ingredientIds: selectedIngredients.filter(ing => !ing.isNew).map(ing => ing.ingredientId),
            recipeIngredients: []
        };

        for (const ing of selectedIngredients) {
            if (ing.isNew) {
                const newIngRes = await createIngredient({
                    name: ing.ingredientName,
                    unitAbbreviation: ing.unitAbbreviation
                });
                const createdIng = newIngRes.data;
                recipeToSend.recipeIngredients.push({
                    ingredientId: createdIng.id,
                    ingredientName: createdIng.name,
                    quantity: ing.quantity,
                    unitAbbreviation: ing.unitAbbreviation
                });
                recipeToSend.ingredientIds.push(createdIng.id);
            } else {
                recipeToSend.recipeIngredients.push({
                    ingredientId: ing.ingredientId,
                    ingredientName: ing.ingredientName,
                    quantity: ing.quantity,
                    unitAbbreviation: ing.unitAbbreviation
                });
            }
        }

        try {
            await updateRecipe(id, recipeToSend);
            navigate(`/recipe/${id}`);
        } catch (err) {
            setError('Error updating recipe');
        }
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!isAuthor) return <Container className="mt-4">You are not allowed to edit this recipe.</Container>;

    return (
        <Container>
            <h1 className="my-4">Edit Recipe</h1>
            <Form onSubmit={handleSubmit}>
                <Row>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Name *</Form.Label>
                            <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                        </Form.Group>
                    </Col>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Cooking time (min) *</Form.Label>
                            <Form.Control type="number" name="cookingTime" value={formData.cookingTime} onChange={handleChange} required />
                        </Form.Group>
                    </Col>
                </Row>
                <Form.Group className="mb-3">
                    <Form.Label>Description</Form.Label>
                    <Form.Control as="textarea" rows={3} name="description" value={formData.description} onChange={handleChange} />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Image URL</Form.Label>
                    <Form.Control name="imageUrl" value={formData.imageUrl} onChange={handleChange} placeholder="https://..." />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>Category *</Form.Label>
                    <Form.Select name="categoryId" value={formData.categoryId} onChange={handleChange} required>
                        {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </Form.Select>
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Ingredients</Form.Label>
                    <div style={{ maxHeight: '400px', overflowY: 'auto', border: '1px solid #ced4da', padding: '12px', borderRadius: '8px' }}>
                        {existingIngredients.map(ing => {
                            const selected = selectedIngredients.find(i => i.ingredientId === ing.id && !i.isNew);
                            return (
                                <div key={ing.id} className="mb-3 p-2 border rounded">
                                    <Form.Check
                                        type="checkbox"
                                        label={`${ing.name} (${ing.unitAbbreviation})`}
                                        checked={!!selected}
                                        onChange={() => handleExistingIngredientToggle(ing)}
                                    />
                                    {selected && (
                                        <div className="mt-2 ms-4">
                                            <Row>
                                                <Col md={6}>
                                                    <Form.Label>Quantity</Form.Label>
                                                    <Form.Control
                                                        type="number"
                                                        step="0.1"
                                                        value={selected.quantity}
                                                        onChange={(e) => handleIngredientChange(ing.id, 'quantity', parseFloat(e.target.value))}
                                                        min="0.1"
                                                    />
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Label>Unit</Form.Label>
                                                    <Form.Select
                                                        value={selected.unitAbbreviation}
                                                        onChange={(e) => handleIngredientChange(ing.id, 'unitAbbreviation', e.target.value)}
                                                    >
                                                        {units.map(unit => (
                                                            <option key={unit.id} value={unit.abbreviation}>
                                                                {unit.name} ({unit.abbreviation})
                                                            </option>
                                                        ))}
                                                    </Form.Select>
                                                </Col>
                                            </Row>
                                        </div>
                                    )}
                                </div>
                            );
                        })}

                        {selectedIngredients.filter(i => i.isNew).map(ing => (
                            <div key={ing.tempId} className="mb-3 p-2 border rounded bg-light">
                                <div className="d-flex justify-content-between align-items-center">
                                    <strong>🆕 {ing.ingredientName}</strong>
                                    <Button
                                        variant="outline-danger"
                                        size="sm"
                                        onClick={() => setSelectedIngredients(selectedIngredients.filter(i => i.tempId !== ing.tempId))}
                                    >
                                        Remove
                                    </Button>
                                </div>
                                <div className="mt-2 ms-4">
                                    <Row>
                                        <Col md={6}>
                                            <Form.Label>Quantity</Form.Label>
                                            <Form.Control
                                                type="number"
                                                step="0.1"
                                                value={ing.quantity}
                                                onChange={(e) => handleIngredientChange(ing.ingredientId, 'quantity', parseFloat(e.target.value))}
                                                min="0.1"
                                            />
                                        </Col>
                                        <Col md={6}>
                                            <Form.Label>Unit</Form.Label>
                                            <Form.Select
                                                value={ing.unitAbbreviation}
                                                onChange={(e) => handleIngredientChange(ing.ingredientId, 'unitAbbreviation', e.target.value)}
                                            >
                                                {units.map(unit => (
                                                    <option key={unit.id} value={unit.abbreviation}>
                                                        {unit.name} ({unit.abbreviation})
                                                    </option>
                                                ))}
                                            </Form.Select>
                                        </Col>
                                    </Row>
                                </div>
                            </div>
                        ))}

                        {!showNewIngredient ? (
                            <Button variant="outline-primary" onClick={() => setShowNewIngredient(true)}>
                                + Add new ingredient
                            </Button>
                        ) : (
                            <div className="mt-2 p-3 border rounded bg-white">
                                <Row>
                                    <Col md={5}>
                                        <Form.Control
                                            placeholder="Ingredient name"
                                            value={newIngredient.name}
                                            onChange={e => setNewIngredient({ ...newIngredient, name: e.target.value })}
                                        />
                                    </Col>
                                    <Col md={3}>
                                        <Form.Control
                                            type="number"
                                            step="0.1"
                                            placeholder="Quantity"
                                            value={newIngredient.quantity}
                                            onChange={e => setNewIngredient({ ...newIngredient, quantity: parseFloat(e.target.value) })}
                                        />
                                    </Col>
                                    <Col md={3}>
                                        <Form.Select
                                            value={newIngredient.unitAbbreviation}
                                            onChange={e => setNewIngredient({ ...newIngredient, unitAbbreviation: e.target.value })}
                                        >
                                            <option value="">Select unit</option>
                                            {units.map(unit => (
                                                <option key={unit.id} value={unit.abbreviation}>
                                                    {unit.name} ({unit.abbreviation})
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Col>
                                    <Col md={1}>
                                        <Button variant="success" onClick={handleAddNewIngredient}>Add</Button>
                                    </Col>
                                </Row>
                                <Button variant="link" size="sm" onClick={() => setShowNewIngredient(false)}>Cancel</Button>
                            </div>
                        )}
                    </div>
                </Form.Group>

                <Button variant="primary" type="submit" className="mt-3">Update Recipe</Button>
            </Form>
        </Container>
    );
}