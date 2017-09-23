package aenadon.wienerlinienalarm.models.wl_metadata;

import android.support.annotation.NonNull;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class Station extends RealmObject {

    @PrimaryKey
    private String id;
    @Required
    private String idForXMLApi;
    @Required
    private String name;
    @Required
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

    public List<Steig> getSteigs() {
        return steigs;
    }

    public void setSteigs(List<Steig> steigs) {
        RealmList<Steig> steigList = new RealmList<>();
        steigList.addAll(steigs);
        this.steigs = steigList;
    }
}
