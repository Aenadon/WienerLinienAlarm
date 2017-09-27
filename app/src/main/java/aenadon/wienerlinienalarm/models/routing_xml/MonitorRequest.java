package aenadon.wienerlinienalarm.models.routing_xml;

import org.simpleframework.xml.Element;

class MonitorRequest {

    @Element(name = "itdServingLines", required = false)
    private ServingLines servingLines;

    ServingLines getServingLines() {
        return servingLines;
    }

    public void setServingLines(ServingLines servingLines) {
        this.servingLines = servingLines;
    }
}
