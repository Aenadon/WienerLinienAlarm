package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.Attribute;

public class DirectionParam {

    @Attribute(name = "direction")
    private String direction;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
