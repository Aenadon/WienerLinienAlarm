package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.Element;

public class MonitorRequest {

    @Element(name = "itdServingLines", required = false)
    private ServingLines servingLines;

    public ServingLines getServingLines() {
        return servingLines;
    }

    public void setServingLines(ServingLines servingLines) {
        this.servingLines = servingLines;
    }
}
