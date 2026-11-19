package core.movements;

import core.common.Position;

/**
 * This class implements a horizontal movement strategy for an object in a 2D space.
 * It calculates the next position of the object based on its current position, speed, and the time step.
 */
public class HorizontalMovement implements MovementStrategy {

    public HorizontalMovement() {}

    @Override
    public Position nextPosition(Position currentPosition, double timeStep, double speed) {
        return new Position(currentPosition.getX() + speed * timeStep, currentPosition.getY());
    }
}
