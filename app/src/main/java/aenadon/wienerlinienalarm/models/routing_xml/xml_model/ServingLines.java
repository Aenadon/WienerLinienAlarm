package aenadon.wienerlinienalarm.models.routing_xml.xml_model;

import org.simpleframework.xml.ElementList;

import java.util.List;

public class ServingLines {

    @ElementList(entry = "itdServingLine", inline = true, required = false)
    private List<ServingLine> lines;

    public List<ServingLine> getLines() {
        return lines;
    }

    public void setLines(List<ServingLine> lines) {
        this.lines = lines;
    }
}
