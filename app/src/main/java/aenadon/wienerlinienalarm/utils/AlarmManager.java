package aenadon.wienerlinienalarm.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.models.Alarm;
import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class AlarmManager {

    private final String LOG_TAG = AlarmManager.class.getSimpleName();
    private final String apikey = BuildConfig.API_TOKEN;

    private PendingIntent getPendingIntent(Context ctx, String uid) {
        Intent i = new Intent(ctx, AlarmReceiver.class);
        i.putExtra(Const.EXTRA_ALARM_ID, uid);
        return PendingIntent.getBroadcast(ctx, Const.REQUEST_ALARM, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void scheduleAlarm(Context ctx, Alarm alarm) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, alarm.getAlarmInstantMillis(), getPendingIntent(ctx, alarm.getId()));
        } else {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, alarm.getAlarmInstantMillis(), getPendingIntent(ctx, alarm.getId()));
        }

        addAlarmToPrefs(ctx, alarm.getId());

        Log.d(LOG_TAG, "Scheduled alarm with id: " + alarm.getId());
    }

    public void cancelAlarm(Context ctx, Alarm alarm) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = getPendingIntent(ctx, alarm.getId());
        pi.cancel();
        alarmManager.cancel(pi);

        removeAlarmFromPrefs(ctx, alarm.getId());

        Log.d(LOG_TAG, "Canceled alarm with id: " + alarm.getId());
    }

    private SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(Const.PREFS_SCHEDULED_ALARMS, Context.MODE_PRIVATE);
    }

    private void addAlarmToPrefs(Context ctx, String uid) {
        SharedPreferences.Editor se = getPrefs(ctx).edit();
        se.putBoolean(uid, true);
        se.apply();
    }

    private void removeAlarmFromPrefs(Context ctx, String uid) {
        SharedPreferences.Editor se = getPrefs(ctx).edit();
        se.remove(uid);
        se.apply();
    }

    // Receives the alarm broadcast
    public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String NOTIFICATION_ID = "NOTIFICATION_ID";

            String alarmId = intent.getStringExtra(Const.EXTRA_ALARM_ID);

            SharedPreferences sp = getPrefs(context);
            int notificationId = sp.getInt(NOTIFICATION_ID, 0);

            Realm.init(context);
            Realm realm = Realm.getDefaultInstance();

            Alarm alarm = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();

            String ringtone = alarm.getChosenRingtone();
            int vibrationMode = alarm.getChosenVibrationMode();

            String stationName = alarm.getStationName();
            String stationId = alarm.getStationId();
            int stationIndex = alarm.getStationArrayIndex();

            String info = "";
            try {
                Response<ResponseBody> response = RetrofitInfo.getRealtimeInfo().create(RetrofitInfo.RealtimeCalls.class).getRealtime(apikey, stationId).execute();
                JSONArray lines = new JSONObject(response.body().string())
                        .getJSONObject("data")
                        .getJSONArray("monitors")
                        .getJSONObject(stationIndex)
                        .getJSONArray("lines");
                for (int i = 0; i < lines.length(); i++) {
                    JSONObject line = lines.getJSONObject(i);
                    info += line.getString("name") + " " + line.getString("towards"); // U6 ALTERLAA
                    JSONObject dep = line.getJSONObject("departures");
                    if (dep.length() == 0 || dep.getJSONArray("departure").length() == 0) {
                        info += "\n";
                    } else {
                        JSONArray departures = dep.getJSONArray("departure");
                        for (int j = 0; j < departures.length(); j++) {
                            JSONObject departureTime = departures.getJSONObject(j).getJSONObject("departureTime");
                            String timeString = (departureTime.has("timeReal")) ? departureTime.getString("timeReal") : departureTime.getString("timePlanned");
                            String time = timeString.substring(11,16);
                            info += " " + time + "\n";
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                info = e.getMessage();
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(context.getString(R.string.app_name) + " - " + stationName)
                            .setContentText(info)
                            .setSound(Uri.parse(ringtone))
                            .setVibrate(new long[]{0, Const.VIBRATION_DURATION[vibrationMode]});

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationId, mBuilder.build());

            // update notification counter. this helps if a user sets alarms
            // only a little apart (so notifications don't override each other)
            sp.edit().putInt(NOTIFICATION_ID, notificationId+1).apply();

            removeAlarmFromPrefs(context, alarmId);
        }
    }

    // Fires on "boot completed" to reschedule all alarms (because they get lost on reboot)
    public class BootReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sp = getPrefs(context);
            Map<String,?> keys = sp.getAll();

            Realm.init(context);
            Realm realm = Realm.getDefaultInstance();

            for (Map.Entry<String,?> entry : keys.entrySet()) {
                // reschedule all alarms
                scheduleAlarm(context, realm.where(Alarm.class).equalTo("id", entry.getKey()).findFirst());
            }
        }
    }

}
