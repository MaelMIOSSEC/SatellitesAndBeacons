package core.elements;

import core.common.Position;

public abstract class BoardElement {
    private String label;
    private Position position;
    private MovementStrategy movementStrategy;

    public BoardElement(
            String label,
            Position position,
            MovementStrategy movementStrategy
    ) {
        this.label = label;
        this.position = position;
        this.movementStrategy = movementStrategy;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public MovementStrategy getMovementStrategy() { return movementStrategy; }
    public void setMovementStrategy(MovementStrategy movementStrategy) { this.movementStrategy = movementStrategy; }
}
