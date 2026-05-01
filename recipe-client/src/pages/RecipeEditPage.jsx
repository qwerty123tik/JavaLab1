import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Form, Button, Container, Row, Col, Alert, Spinner, Modal } from 'react-bootstrap';
import { getRecipeById, updateRecipe, getCategories, getIngredients, getUnits, createIngredient, createCategory, createUnit } from '../api/recipeApi';
import { useAuth } from '../context/AuthContext';

export default function RecipeEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [categories, setCategories] = useState([]);
    const [existingIngredients, setExistingIngredients] = useState([]);
    const [units, setUnits] = useState([]);
    const [searchIngredient, setSearchIngredient] = useState('');
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

    // Категория
    const [showNewCategoryModal, setShowNewCategoryModal] = useState(false);
    const [newCategory, setNewCategory] = useState({ name: '', description: '' });

    // Единица измерения
    const [showNewUnitModal, setShowNewUnitModal] = useState(false);
    const [newUnit, setNewUnit] = useState({ name: '', abbreviation: '' });

    const filteredIngredients = existingIngredients.filter(ing =>
        ing.name.toLowerCase().includes(searchIngredient.toLowerCase())
    );

    useEffect(() => {
        Promise.all([getRecipeById(id), getCategories(), getIngredients(), getUnits()])
            .then(([recipeRes, catRes, ingRes, unitRes]) => {
                const recipe = recipeRes.data;
                if (!user || user.id !== recipe.authorId) {
                    setError('Вы не являетесь автором этого рецепта');
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
            .catch(() => setError('Не удалось загрузить рецепт'));
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
            setError('Введите название ингредиента');
            return;
        }
        const duplicate = existingIngredients.some(ing => ing.name.toLowerCase() === newIngredient.name.trim().toLowerCase());
        if (duplicate) {
            setError(`Ингредиент "${newIngredient.name}" уже существует. Выберите его из списка.`);
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

    const handleCreateCategory = async () => {
        if (!newCategory.name.trim()) {
            setError('Введите название категории');
            return;
        }
        try {
            const created = await createCategory({ name: newCategory.name, description: newCategory.description });
            const newCat = created.data;
            setCategories([...categories, newCat]);
            setFormData(prev => ({ ...prev, categoryId: newCat.id }));
            setShowNewCategoryModal(false);
            setNewCategory({ name: '', description: '' });
        } catch (err) {
            setError('Не удалось создать категорию');
        }
    };

    const handleCreateUnit = async () => {
        if (!newUnit.name.trim() || !newUnit.abbreviation.trim()) {
            setError('Введите название и аббревиатуру');
            return;
        }
        try {
            const created = await createUnit({ name: newUnit.name, abbreviation: newUnit.abbreviation });
            const newUnitObj = created.data;
            setUnits([...units, newUnitObj]);
            setShowNewUnitModal(false);
            setNewUnit({ name: '', abbreviation: '' });
        } catch (err) {
            setError('Не удалось создать единицу измерения');
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (selectedIngredients.length === 0) {
            setError('Выберите хотя бы один ингредиент');
            return;
        }
        const invalid = selectedIngredients.some(ing => !ing.quantity || ing.quantity <= 0);
        if (invalid) {
            setError('Введите корректное количество (>0) для каждого ингредиента');
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
            setError('Ошибка при обновлении рецепта');
        }
    };

    if (loading) return <Container className="mt-4"><Spinner animation="border" /></Container>;
    if (error) return <Container className="mt-4"><Alert variant="danger">{error}</Alert></Container>;
    if (!isAuthor) return <Container className="mt-4">Вы не можете редактировать этот рецепт.</Container>;

    return (
        <Container>
            <h1 className="my-4">Редактировать рецепт</h1>
            <Form onSubmit={handleSubmit}>
                <Row>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Название *</Form.Label>
                            <Form.Control name="name" value={formData.name} onChange={handleChange} required />
                        </Form.Group>
                    </Col>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Время приготовления (мин) *</Form.Label>
                            <Form.Control type="number" name="cookingTime" value={formData.cookingTime} onChange={handleChange} required />
                        </Form.Group>
                    </Col>
                </Row>
                <Form.Group className="mb-3">
                    <Form.Label>Описание</Form.Label>
                    <Form.Control as="textarea" rows={3} name="description" value={formData.description} onChange={handleChange} />
                </Form.Group>
                <Form.Group className="mb-3">
                    <Form.Label>URL изображения</Form.Label>
                    <Form.Control name="imageUrl" value={formData.imageUrl} onChange={handleChange} placeholder="https://..." />
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Категория *</Form.Label>
                    <div className="d-flex gap-2">
                        <Form.Select name="categoryId" value={formData.categoryId} onChange={handleChange} required className="flex-grow-1">
                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </Form.Select>
                        <Button variant="outline-primary" onClick={() => setShowNewCategoryModal(true)}>+ Новая</Button>
                    </div>
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Ингредиенты</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="🔍 Поиск ингредиентов..."
                        value={searchIngredient}
                        onChange={e => setSearchIngredient(e.target.value)}
                        className="mb-2"
                    />
                    <div style={{ maxHeight: '400px', overflowY: 'auto', border: '1px solid #ced4da', padding: '12px', borderRadius: '8px' }}>
                        {filteredIngredients.map(ing => {
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
                                                    <Form.Label>Количество</Form.Label>
                                                    <Form.Control
                                                        type="number"
                                                        step="0.1"
                                                        value={selected.quantity}
                                                        onChange={(e) => handleIngredientChange(ing.id, 'quantity', parseFloat(e.target.value))}
                                                        min="0.1"
                                                    />
                                                </Col>
                                                <Col md={6}>
                                                    <Form.Label>Единица</Form.Label>
                                                    <div className="d-flex gap-2">
                                                        <Form.Select
                                                            value={selected.unitAbbreviation}
                                                            onChange={(e) => handleIngredientChange(ing.id, 'unitAbbreviation', e.target.value)}
                                                            className="flex-grow-1"
                                                        >
                                                            {units.map(unit => (
                                                                <option key={unit.id} value={unit.abbreviation}>
                                                                    {unit.name} ({unit.abbreviation})
                                                                </option>
                                                            ))}
                                                        </Form.Select>
                                                        <Button variant="outline-primary" size="sm" onClick={() => setShowNewUnitModal(true)}>+</Button>
                                                    </div>
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
                                        Удалить
                                    </Button>
                                </div>
                                <div className="mt-2 ms-4">
                                    <Row>
                                        <Col md={6}>
                                            <Form.Label>Количество</Form.Label>
                                            <Form.Control
                                                type="number"
                                                step="0.1"
                                                value={ing.quantity}
                                                onChange={(e) => handleIngredientChange(ing.ingredientId, 'quantity', parseFloat(e.target.value))}
                                                min="0.1"
                                            />
                                        </Col>
                                        <Col md={6}>
                                            <Form.Label>Единица</Form.Label>
                                            <div className="d-flex gap-2">
                                                <Form.Select
                                                    value={ing.unitAbbreviation}
                                                    onChange={(e) => handleIngredientChange(ing.ingredientId, 'unitAbbreviation', e.target.value)}
                                                    className="flex-grow-1"
                                                >
                                                    {units.map(unit => (
                                                        <option key={unit.id} value={unit.abbreviation}>
                                                            {unit.name} ({unit.abbreviation})
                                                        </option>
                                                    ))}
                                                </Form.Select>
                                                <Button variant="outline-primary" size="sm" onClick={() => setShowNewUnitModal(true)}>+</Button>
                                            </div>
                                        </Col>
                                    </Row>
                                </div>
                            </div>
                        ))}

                        {!showNewIngredient ? (
                            <Button variant="outline-primary" onClick={() => setShowNewIngredient(true)}>
                                + Добавить новый ингредиент
                            </Button>
                        ) : (
                            <div className="mt-2 p-3 border rounded bg-white">
                                <Row>
                                    <Col md={5}>
                                        <Form.Control
                                            placeholder="Название ингредиента"
                                            value={newIngredient.name}
                                            onChange={e => setNewIngredient({ ...newIngredient, name: e.target.value })}
                                        />
                                    </Col>
                                    <Col md={3}>
                                        <Form.Control
                                            type="number"
                                            step="0.1"
                                            placeholder="Количество"
                                            value={newIngredient.quantity}
                                            onChange={e => setNewIngredient({ ...newIngredient, quantity: parseFloat(e.target.value) })}
                                        />
                                    </Col>
                                    <Col md={3}>
                                        <div className="d-flex gap-2">
                                            <Form.Select
                                                value={newIngredient.unitAbbreviation}
                                                onChange={e => setNewIngredient({ ...newIngredient, unitAbbreviation: e.target.value })}
                                                className="flex-grow-1"
                                            >
                                                <option value="">Выберите единицу</option>
                                                {units.map(unit => (
                                                    <option key={unit.id} value={unit.abbreviation}>
                                                        {unit.name} ({unit.abbreviation})
                                                    </option>
                                                ))}
                                            </Form.Select>
                                            <Button variant="outline-primary" size="sm" onClick={() => setShowNewUnitModal(true)}>+</Button>
                                        </div>
                                    </Col>
                                    <Col md={1}>
                                        <Button variant="success" onClick={handleAddNewIngredient}>Добавить</Button>
                                    </Col>
                                </Row>
                                <Button variant="link" size="sm" onClick={() => setShowNewIngredient(false)}>Отмена</Button>
                            </div>
                        )}
                    </div>
                </Form.Group>

                <Button variant="primary" type="submit" className="mt-4 mb-4">Сохранить изменения</Button>
            </Form>

            {/* Модальное окно для категории */}
            <Modal show={showNewCategoryModal} onHide={() => setShowNewCategoryModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Новая категория</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Group className="mb-3">
                        <Form.Label>Название категории *</Form.Label>
                        <Form.Control value={newCategory.name} onChange={e => setNewCategory({ ...newCategory, name: e.target.value })} />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Описание</Form.Label>
                        <Form.Control as="textarea" rows={2} value={newCategory.description} onChange={e => setNewCategory({ ...newCategory, description: e.target.value })} />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowNewCategoryModal(false)}>Отмена</Button>
                    <Button variant="primary" onClick={handleCreateCategory}>Создать</Button>
                </Modal.Footer>
            </Modal>

            {/* Модальное окно для единицы измерения */}
            <Modal show={showNewUnitModal} onHide={() => setShowNewUnitModal(false)}>
                <Modal.Header closeButton>
                    <Modal.Title>Новая единица измерения</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Group className="mb-3">
                        <Form.Label>Название *</Form.Label>
                        <Form.Control value={newUnit.name} onChange={e => setNewUnit({ ...newUnit, name: e.target.value })} />
                    </Form.Group>
                    <Form.Group className="mb-3">
                        <Form.Label>Аббревиатура *</Form.Label>
                        <Form.Control value={newUnit.abbreviation} onChange={e => setNewUnit({ ...newUnit, abbreviation: e.target.value })} />
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowNewUnitModal(false)}>Отмена</Button>
                    <Button variant="primary" onClick={handleCreateUnit}>Создать</Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}