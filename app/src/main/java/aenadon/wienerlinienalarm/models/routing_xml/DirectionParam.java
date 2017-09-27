package aenadon.wienerlinienalarm.models.routing_xml;

import org.simpleframework.xml.Attribute;

class DirectionParam {

    @Attribute(name = "direction")
    private String direction;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
