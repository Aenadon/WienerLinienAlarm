package aenadon.wienerlinienalarm.enums;

import java.util.Arrays;
import java.util.List;

public enum TransportType {
    BUS("ptBusCity"),
    BUS_NIGHT("ptBusNight"),
    TRAM("ptTram"),
    TRAM_VRT("ptTramVRT"),
    TRAM_WLB("ptTramWLB"),
    TRAIN_S("ptTrainS"),
    METRO("ptMetro");

    private String typeString;
    private static List<TransportType> transportTypeList = Arrays.asList(TransportType.values());

    TransportType(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }

    public static TransportType findByTypeString(String typeString) {
        for (TransportType type : transportTypeList) {
            if (type.typeString.equals(typeString)) {
                return type;
            }
        }
        return null;
    }
}
