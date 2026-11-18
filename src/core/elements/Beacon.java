package core.elements;

import core.common.Position;
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

    public void move(Position position, MovementStrategy movementStrategy) {
        
    }
}
