package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "itdRequest", strict = false)
public class RoutingXMLRequest {

    @Element(name = "itdDepartureMonitorRequest")
    private MonitorRequest monitorRequest;

    public MonitorRequest getMonitorRequest() {
        return monitorRequest;
    }

    public void setMonitorRequest(MonitorRequest monitorRequest) {
        this.monitorRequest = monitorRequest;
    }
}
