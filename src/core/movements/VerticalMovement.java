package core.movements;

import core.common.Position;

public class VerticalMovement implements MovementStrategy {

    public VerticalMovement() {}

    @Override
    public Position nextPosition(Position current, double timeStep, double speed) {
        return new Position(current.getX(), current.getY() + speed * timeStep);
    }
}
