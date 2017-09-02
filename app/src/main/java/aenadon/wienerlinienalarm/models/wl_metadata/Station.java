package aenadon.wienerlinienalarm.models.wl_metadata;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Station extends RealmObject {

    @PrimaryKey
    private String id;
    private String idForXMLApi;
    private String name;
    private String city;
    private RealmList<Steig> steigs;

    public Station() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdForXMLApi() {
        return idForXMLApi;
    }

    public void setIdForXMLApi(String idForXMLApi) {
        this.idForXMLApi = idForXMLApi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public RealmList<Steig> getSteigs() {
        return steigs;
    }

    public void setSteigs(RealmList<Steig> steigs) {
        this.steigs = steigs;
    }
}
