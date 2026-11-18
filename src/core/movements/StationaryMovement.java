package core.movements;

import core.common.Position;

public class StationaryMovement implements MovementStrategy {

    @Override
    public Position nextPosition(Position current, double timeStep) {
        return current;
    }
}
