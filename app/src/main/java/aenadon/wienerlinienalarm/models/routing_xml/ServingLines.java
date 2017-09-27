package aenadon.wienerlinienalarm.models.routing_xml;

import org.simpleframework.xml.ElementList;

import java.util.List;

class ServingLines {

    @ElementList(entry = "itdServingLine", inline = true)
    private List<ServingLine> lines;

    List<ServingLine> getLines() {
        return lines;
    }

    public void setLines(List<ServingLine> lines) {
        this.lines = lines;
    }
}
