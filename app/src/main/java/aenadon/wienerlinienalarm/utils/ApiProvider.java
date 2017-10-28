package aenadon.wienerlinienalarm.utils;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import aenadon.wienerlinienalarm.models.routing_xml.RoutingXMLRequest;
import aenadon.wienerlinienalarm.realtime.RealtimeData;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ApiProvider {

    private static final Retrofit csvApiBase = new Retrofit.Builder()
            .baseUrl("http://data.wien.gv.at/csv/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build();

    public static CSVApi getCSVApi() {
        return csvApiBase.create(CSVApi.class);
    }

    private static final Retrofit realtimeApiBase = new Retrofit.Builder()
            .baseUrl("https://www.wienerlinien.at/ogd_realtime/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build();

    public static RealtimeApi getRealtimeApi() {
        return realtimeApiBase.create(RealtimeApi.class);
    }

    private static final Retrofit routingApiBase = new Retrofit.Builder()
            .baseUrl("http://www.wienerlinien.at/ogd_routing/")
            // to prevent crash on empty tags
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
            .build();

    public static RoutingApi getRoutingApi() {
        return routingApiBase.create(RoutingApi.class);
    }

    public interface CSVApi {

        @GET("wienerlinien-ogd-linien.csv")
        Call<String> getLinienCSV();

        @GET("wienerlinien-ogd-haltestellen.csv")
        Call<String> getHaltestellenCSV();

        @GET("wienerlinien-ogd-steige.csv")
        Call<String> getSteigeCSV();

        @GET("wienerlinien-ogd-version.csv")
        Call<String> getVersionCSV();

    }

    public interface RealtimeApi {

        @GET("monitor")
        Call<RealtimeData> getRealtime(@Query("sender") String apiKey, @Query("rbl") String rbl);

    }

    public interface RoutingApi {

        @GET("XML_DM_REQUEST?type_dm=any")
        Call<RoutingXMLRequest> getXMLStationInfo(@Query("name_dm") String stationXmlId);

    }
}
