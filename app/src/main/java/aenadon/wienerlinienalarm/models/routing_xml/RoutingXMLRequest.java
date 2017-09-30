package aenadon.wienerlinienalarm.models.routing_xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

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

    public List<XmlSteig> getStationLines() {
        ServingLines xmlServingLines = monitorRequest.getServingLines();
        List<XmlSteig> flatListSteigs = new ArrayList<>();
        if (xmlServingLines != null) {
            List<ServingLine> servingLineList = monitorRequest.getServingLines().getLines();
            if (servingLineList != null) {
                for (ServingLine line : servingLineList) {
                    XmlSteig flattedLine = new XmlSteig();
                    flattedLine.setLine(line.getLine());
                    flattedLine.setDirection(line.getDirectionParam().getDirection());
                    flattedLine.setDestination(line.getDestination().replace("Wien ", ""));
                    flatListSteigs.add(flattedLine);
                }
            }
        }
        return flatListSteigs;
    }
}
