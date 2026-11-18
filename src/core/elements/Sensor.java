package core.elements;

public abstract class Sensor {
    private String label;

    public Sensor(String label) { this.label = label; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
