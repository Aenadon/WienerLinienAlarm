package aenadon.wienerlinienalarm.enums;

public enum TransportType {
    BUS("ptBusCity"),
    BUS_NIGHT("ptBusNight"),
    TRAM("ptTram"),
    TRAM_VRT("ptTramVRT"),
    TRAM_WLB("ptTramWLB"),
    TRAIN_S("ptTrainS"),
    METRO("ptMetro");

    private String typeString;

    TransportType(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }

    public static TransportType findByTypeString(String typeString) {
        for (TransportType type : values()) {
            if (type.typeString.equals(typeString)) {
                return type;
            }
        }
        return null;
    }
}
