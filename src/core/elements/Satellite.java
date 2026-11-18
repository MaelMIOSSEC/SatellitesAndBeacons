package core.elements;

public class Satellite extends BoardElement {
    private Boolean isAvailable;

    public Satellite(
            String label,
            Position position,
            MovementStrategy movementStrategy,
            Boolean isAvailable
    ) {
        super(label, position, movementStrategy);

        this.isAvailable = isAvailable;
    }

    public Boolean getAvailable() { return isAvailable; }
    public void setAvailable(Boolean available) { isAvailable = available; }
}
