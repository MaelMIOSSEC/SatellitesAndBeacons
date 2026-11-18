package core.elements;

public class Measure {
    private Position position;
    private Integer time;
    private MeasureValue value;

    public Measure(
            Position position,
            Integer time,
            MeasureValue value
    ) {
        this.position = position;
        this.time = time;
        this.value = value;
    }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public MeasureValue getValue() { return value; }
    public void setValue(MeasureValue value) { this.value = value; }
}
