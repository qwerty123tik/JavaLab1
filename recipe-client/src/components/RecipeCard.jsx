import { Card, Button, OverlayTrigger, Tooltip } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { FaStar, FaStarHalfAlt, FaRegStar, FaEdit, FaTrashAlt } from 'react-icons/fa';
import { useAuth } from '../context/AuthContext';

export default function RecipeCard({ recipe, onDelete }) {
    const { user } = useAuth();
    const isOwner = user && user.id === recipe.authorId;

    const renderStars = (rating) => {
        if (!rating || rating === 0) return <span className="text-muted">Нет оценок</span>;
        const full = Math.floor(rating);
        const half = rating % 1 >= 0.5;
        const empty = 5 - full - (half ? 1 : 0);
        return (
            <>
                {[...Array(full)].map((_, i) => <FaStar key={i} color="#FFD166" size={18} />)}
                {half && <FaStarHalfAlt color="#FFD166" size={18} />}
                {[...Array(empty)].map((_, i) => <FaRegStar key={i} color="#FFD166" size={18} />)}
            </>
        );
    };

    const imageUrl = recipe.imageUrl || `https://picsum.photos/id/${(recipe.id % 100) + 1}/300/200`;

    return (
        <Card className="recipe-card h-100">
            <Card.Img variant="top" src={imageUrl} alt={recipe.name} className="recipe-card-img" />
            <Card.Body className="d-flex flex-column">
                <Card.Title className="card-title" style={{ minHeight: '4rem', lineHeight: '1.4' }}>
                    {recipe.name}
                </Card.Title>
                <div className="rating mb-2">{renderStars(recipe.averageRating)}</div>
                <div className="mt-auto d-flex justify-content-between align-items-center flex-wrap gap-2">
                    <Link to={`/recipe/${recipe.id}`}>
                        <Button variant="primary" size="sm">Подробнее</Button>
                    </Link>
                    {isOwner && (
                        <div className="d-flex gap-2">
                            <Link to={`/edit/${recipe.id}`}>
                                <OverlayTrigger placement="top" overlay={<Tooltip>Редактировать</Tooltip>}>
                                    <Button variant="outline-secondary" size="sm">
                                        <FaEdit />
                                    </Button>
                                </OverlayTrigger>
                            </Link>
                            <OverlayTrigger placement="top" overlay={<Tooltip>Удалить</Tooltip>}>
                                <Button variant="outline-danger" size="sm" onClick={() => onDelete(recipe.id)}>
                                    <FaTrashAlt />
                                </Button>
                            </OverlayTrigger>
                        </div>
                    )}
                </div>
            </Card.Body>
        </Card>
    );
}