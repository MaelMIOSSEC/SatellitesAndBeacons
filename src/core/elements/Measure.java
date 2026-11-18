package core.elements;

import core.common.Position;

public class Measure {
    private Position position;
    private Double time;
    private MeasureValue value;

    public Measure(
            Position position,
            Double time,
            MeasureValue value
    ) {
        this.position = position;
        this.time = time;
        this.value = value;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public MeasureValue getValue() {
        return value;
    }

    public void setValue(MeasureValue value) {
        this.value = value;
    }
}
