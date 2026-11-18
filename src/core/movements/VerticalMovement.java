package core.movements;

import core.common.Position;

public class VerticalMovement implements MovementStrategy {

    private final double speed;

    public VerticalMovement(double speed) {
        this.speed = speed;
    }

    @Override
    public Position nextPosition(Position current, double timeStep) {
        return new Position(current.getX(), current.getY() + speed * timeStep);
    }
}
