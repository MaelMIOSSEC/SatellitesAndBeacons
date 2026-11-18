package core.values;

import core.common.Direction;

public class CurrentValue {
    private Double speed;
    private Direction direction;

    public CurrentValue(Double speed, Direction direction) {
        this.speed = speed;
        this.direction = direction;
    }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed;}

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
}
