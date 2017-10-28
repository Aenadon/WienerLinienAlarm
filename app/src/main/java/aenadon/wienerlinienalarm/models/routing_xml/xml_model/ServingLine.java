package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class ServingLine {

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

    public DirectionParam getDirectionParam() {
        return directionParam;
    }

    public void setDirectionParam(DirectionParam directionParam) {
        this.directionParam = directionParam;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
