package aenadon.wienerlinienalarm.models.routing_xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

class ServingLine {

    @Attribute(name = "number")
    private String line;

    @Element(name = "motDivaParams")
    private DirectionParam directionParam;

    @Attribute(name = "direction")
    private String destination;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    DirectionParam getDirectionParam() {
        return directionParam;
    }

    public void setDirectionParam(DirectionParam directionParam) {
        this.directionParam = directionParam;
    }

    String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
