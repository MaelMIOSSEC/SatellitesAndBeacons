package core.movements;

import core.common.Position;

public class SinusoidalMovement implements MovementStrategy {
    /**
     * Horizontal speed (unit per second)
     */
    private final double speedX;

    /**
     * wave height (units)
     */
    private final double amplitude;

    /**
     * oscillations per second
     */
    private final double frequency;

    /**
     * Central line, memorized on the first call
     */
    private Double baseY = null;

    /**
     * Cumulated time since the start of the movement (in seconds)
     */
    private double elapsed = 0;

    /**
     * Constructor for SinusoidalMovement.
     *
     * @param speedX    the horizontal speed of the movement (units per second)
     * @param amplitude the amplitude of the sinusoidal movement (units)
     * @param frequency the frequency of the sinusoidal movement (oscillations per second)
     */
    public SinusoidalMovement(double speedX, double amplitude, double frequency) {
        this.speedX = speedX;
        this.amplitude = amplitude;
        this.frequency = frequency;
    }

    @Override
    public Position nextPosition(Position current, double timeStep) {
        if (baseY == null) {
            baseY = current.getY();
        }
        elapsed += timeStep;

        double newX = current.getX() + speedX * timeStep;
        double newY = baseY + amplitude * Math.sin(2 * Math.PI * frequency * elapsed);

        return new Position(newX, newY);
    }
}