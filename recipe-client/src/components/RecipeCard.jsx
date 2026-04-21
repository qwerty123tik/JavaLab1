import { Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RecipeCard({ recipe, onDelete }) {
    const { user } = useAuth();
    const isOwner = user && user.id === recipe.authorId;

    const renderStars = (rating) => {
        if (!rating || rating === 0) return <span className="text-muted">No ratings</span>;
        const full = Math.floor(rating);
        const half = rating % 1 >= 0.5;
        const empty = 5 - full - (half ? 1 : 0);
        return (
            <>
                {'⭐'.repeat(full)}
                {half && '✨'}
                {'☆'.repeat(empty)}
            </>
        );
    };

    const imageUrl = recipe.imageUrl || `https://picsum.photos/id/${(recipe.id % 100) + 1}/300/200`;

    return (
        <Card className="recipe-card h-100">
            <Card.Img variant="top" src={imageUrl} alt={recipe.name} className="recipe-card-img" />
            <Card.Body>
                <Card.Title className="card-title">{recipe.name}</Card.Title>
                <div className="rating mb-2">{renderStars(recipe.averageRating)}</div>
                <div className="d-flex justify-content-between align-items-center mt-2">
                    <Link to={`/recipe/${recipe.id}`}>
                        <Button variant="primary" size="sm">Details</Button>
                    </Link>
                    {isOwner && (
                        <div>
                            <Link to={`/edit/${recipe.id}`}>
                                <Button variant="secondary" size="sm" className="me-2">Edit</Button>
                            </Link>
                            <Button variant="danger" size="sm" onClick={() => onDelete(recipe.id)}>Delete</Button>
                        </div>
                    )}
                </div>
            </Card.Body>
        </Card>
    );
}