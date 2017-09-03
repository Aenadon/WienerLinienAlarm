package aenadon.wienerlinienalarm.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
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
            .build();

    public static RealtimeApi getRealtimeApi() {
        return realtimeApiBase.create(RealtimeApi.class);
    }

    public static RoutingApi getRoutingApi() {
        return new Retrofit.Builder()
                .baseUrl("http://www.wienerlinien.at/ogd_routing/")
                //TODO .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
                .create(RoutingApi.class);
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
        Call<ResponseBody> getRealtime(@Query("sender") String apiKey, @Query("rbl") String rbl);

    }

    public interface RoutingApi {
        // TODO
    }

}
