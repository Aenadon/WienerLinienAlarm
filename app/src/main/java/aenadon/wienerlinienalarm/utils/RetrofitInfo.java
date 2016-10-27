package aenadon.wienerlinienalarm.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

// This is a class containing the API calls

public class RetrofitInfo {

    private static Retrofit csvInfo = new Retrofit.Builder()
            .baseUrl("http://data.wien.gv.at/csv/")
            .build();

    public static Retrofit getCSVInfo() {
        return csvInfo;
    }

    private static Retrofit realtimeInfo = new Retrofit.Builder()
            .baseUrl("https://www.wienerlinien.at/ogd_realtime/")
            .build();

    public static Retrofit getRealtimeInfo() {
        return realtimeInfo;
    }

    public interface CSVCalls {

        @GET("wienerlinien-ogd-haltestellen.csv")
        Call<ResponseBody> getHaltestellenCSV();

        @GET("wienerlinien-ogd-steige.csv")
        Call<ResponseBody> getSteigeCSV();

        @GET("wienerlinien-ogd-version.csv")
        Call<ResponseBody> getVersionCSV();

    }

    public interface RealtimeCalls {

        @GET("monitor")
        Call<ResponseBody> getRealtime(@Query("sender") String apiKey, @Query("rbl") String rbl);

    }


}
