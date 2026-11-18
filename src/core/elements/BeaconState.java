package core.elements;

public interface BeaconState {
    default void update(Beacon beacon, Double dt) {}
}
