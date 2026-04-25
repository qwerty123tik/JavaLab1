import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Container, Row, Col, Spinner, Alert, Button, Pagination } from 'react-bootstrap';
import { searchRecipesNative, getReviewsByRecipe, deleteRecipe } from '../api/recipeApi';
import RecipeCard from '../components/RecipeCard';
import FilterBar from '../components/FilterBar';
import { useAuth } from '../context/AuthContext'; // <-- добавлено

export default function RecipeListPage() {
    const { user } = useAuth(); // <-- добавлено
    const [recipes, setRecipes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({ ingredient: '', category: '', title: '' });
    const [page, setPage] = useState(0);
    const [size] = useState(9);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const loadRecipes = async () => {
        setLoading(true);
        try {
            const res = await searchRecipesNative(
                filters.ingredient,
                filters.category,
                filters.title,
                page,
                size,
                'name,asc'
            );
            const recipesData = res.data.content || [];
            setTotalPages(res.data.totalPages || 0);
            setTotalElements(res.data.totalElements || 0);
            const withRating = await Promise.all(recipesData.map(async (recipe) => {
                try {
                    const reviewsRes = await getReviewsByRecipe(recipe.id);
                    const reviews = reviewsRes.data || [];
                    const avg = reviews.length ? reviews.reduce((s, r) => s + r.rating, 0) / reviews.length : 0;
                    return { ...recipe, averageRating: avg };
                } catch {
                    return { ...recipe, averageRating: 0 };
                }
            }));
            setRecipes(withRating);
        } catch (err) {
            setError('Error loading recipes');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRecipes();
    }, [filters, page]);

    const handlePageChange = (newPage) => (newPage >= 0 && newPage < totalPages) && setPage(newPage);
    const handleSearch = () => { setPage(0); loadRecipes(); };

    const handleDelete = async (id) => {
        if (window.confirm('Удалить рецепт?')) {
            try {
                await deleteRecipe(id);
                loadRecipes();
            } catch (err) {
                alert('Ошибка удаления');
            }
        }
    };

    return (
        <>
            <div className="home-hero">
                <Container>
                    <h1>Рецепты со всего мира</h1>
                    <FilterBar filters={filters} setFilters={setFilters} onSearch={handleSearch} />
                </Container>
            </div>
            <div className="bg-warm py-4">
                <Container>
                    {/* Кнопка добавления – только для авторизованных */}
                    {user && (
                        <div className="d-flex justify-content-end mb-3">
                            <Link to="/create">
                                <Button variant="primary" className="rounded-pill">+ Добавить рецепт</Button>
                            </Link>
                        </div>
                    )}
                    {loading && <Spinner animation="border" className="d-block mx-auto" />}
                    {error && <Alert variant="danger">{error}</Alert>}
                    <Row>
                        {recipes.map(recipe => (
                            <Col key={recipe.id} sm={12} md={6} lg={4} className="mb-4">
                                <RecipeCard recipe={recipe} onDelete={handleDelete} />
                            </Col>
                        ))}
                    </Row>
                    {!loading && totalPages > 0 && (
                        <div className="d-flex justify-content-between align-items-center mt-4">
                            <span className="text-muted">Всего рецептов: {totalElements}, страница {page+1} из {totalPages}</span>
                            <Pagination>
                                <Pagination.Prev onClick={() => handlePageChange(page-1)} disabled={page===0} />
                                <Pagination.Item active>{page+1}</Pagination.Item>
                                <Pagination.Next onClick={() => handlePageChange(page+1)} disabled={page>=totalPages-1} />
                            </Pagination>
                        </div>
                    )}
                </Container>
            </div>
        </>
    );
}