package aenadon.wienerlinienalarm.models.routing_xml;

public class XmlSteig {

    private String line;
    private String direction;
    private String destination;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDestination() {
        return destination;
    }

    void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return line + " " + destination;
    }
}
