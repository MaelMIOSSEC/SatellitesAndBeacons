package core.movements;

import core.common.Position;

/**
 * Interface for movement strategies.
 * Implementations of this interface define how an object moves in a 2D space over time.
 */
public interface MovementStrategy {

    /**
     * Calculates the next position of an object based on its current position and the elapsed time step.
     *
     * @param currentPosition the current position of the object
     * @param timeStep        the time step (in seconds) since the last position update
     * @return the new position of the object after applying the movement strategy
     */
    Position nextPosition(Position currentPosition, double timeStep, double speed);
}
