package core.elements;

import core.common.Position;
import core.exceptions.PositionInvalidException;
import core.movements.MovementStrategy;
import core.states.CollectingState;

import java.util.ArrayList;
import java.util.List;

public class Beacon extends BoardElement {
    private BeaconState state;
    private Integer capacity;
    private List<Measure> measures;
    private List<Sensor> sensors;

    public Beacon(
            String label,
            Position position,
            MovementStrategy movementStrategy,
            List<Sensor> sensors
    ) {
        super(label, position, movementStrategy);

        this.state = new CollectingState();
        this.capacity = 100;
        this.measures = new ArrayList<Measure>();
        this.sensors = sensors;
    }

    public BeaconState getState() {
        return state;
    }

    public void setState(BeaconState state) {
        this.state = state;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public boolean isPositionInvalid(Position position, Board board) {
        return position.getX() < 0 || position.getX() >= board.getWidth() ||
                position.getY() < 0 || position.getY() >= board.getHeight();
    }

    public void move(double dt, Board board) {
        MovementStrategy beaconMovementStrategy = this.getMovementStrategy();
        if (beaconMovementStrategy == null)
            throw new NullPointerException("No specific movement strategy for this tag.");

        Position nextPosition = beaconMovementStrategy.nextPosition(this.getPosition(), dt);

        if (this.isPositionInvalid(nextPosition, board))
            throw new PositionInvalidException("The position you are in or trying to move into is invalid.");

        this.setPosition(nextPosition);
    }
}
