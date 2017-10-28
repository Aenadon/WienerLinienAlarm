package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.models.routing_xml.XmlSteig;
import aenadon.wienerlinienalarm.models.routing_xml.xml_model.MonitorRequest;
import aenadon.wienerlinienalarm.models.routing_xml.xml_model.ServingLine;
import aenadon.wienerlinienalarm.models.routing_xml.xml_model.ServingLines;

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
