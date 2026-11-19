package core.elements;

import core.common.Position;
import core.movements.MovementStrategy;

public abstract class BoardElement {
    private String label;
    private Position position;
    private MovementStrategy movementStrategy;
    private double speed;

    public BoardElement(
            String label,
            Position position,
            MovementStrategy movementStrategy,
            double speed
    ) {
        this.label = label;
        this.position = position;
        this.movementStrategy = movementStrategy;
        this.speed = speed;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public MovementStrategy getMovementStrategy() {
        return movementStrategy;
    }

    public void setMovementStrategy(MovementStrategy movementStrategy) {
        this.movementStrategy = movementStrategy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}