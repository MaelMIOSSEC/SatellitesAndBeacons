package core.elements;

import core.common.Position;
import core.movements.MovementStrategy;

public class Satellite extends BoardElement {
    private Boolean isAvailable;

    public Satellite(
            String label,
            Position position,
            MovementStrategy movementStrategy,
            double speed,
            Boolean isAvailable
    ) {
        super(label, position, movementStrategy, speed);

        this.isAvailable = isAvailable;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
