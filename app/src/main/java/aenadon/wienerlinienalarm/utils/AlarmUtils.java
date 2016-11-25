package aenadon.wienerlinienalarm.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmUtils {

    private static final String LOG_TAG = AlarmUtils.class.getSimpleName();
    private static final String apikey = BuildConfig.API_TOKEN;

    private static PendingIntent getPendingIntent(Context ctx, String uid) {
        Intent i = new Intent(ctx, AlarmReceiver.class);
        i.putExtra(Const.EXTRA_ALARM_ID, uid);
        return PendingIntent.getBroadcast(ctx, Const.REQUEST_ALARM, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void scheduleAlarm(Context ctx, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getAlarmInstantMillis(), getPendingIntent(ctx, alarm.getId()));
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getAlarmInstantMillis(), getPendingIntent(ctx, alarm.getId()));
        }

        addAlarmToPrefs(ctx, alarm.getId());

        Log.d(LOG_TAG, "Scheduled alarm with id: " + alarm.getId());
    }

    public static void cancelAlarm(Context ctx, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = getPendingIntent(ctx, alarm.getId());
        pi.cancel();
        alarmManager.cancel(pi);

        removeAlarmFromPrefs(ctx, alarm.getId());

        Log.d(LOG_TAG, "Canceled alarm with id: " + alarm.getId());
    }

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(Const.PREFS_SCHEDULED_ALARMS, Context.MODE_PRIVATE);
    }

    private static void addAlarmToPrefs(Context ctx, String uid) {
        SharedPreferences.Editor se = getPrefs(ctx).edit();
        se.putBoolean(uid, true);
        se.apply();
        Log.d(LOG_TAG, "Alarm with id " + uid + " added to prefs");
    }

    private static void removeAlarmFromPrefs(Context ctx, String uid) {
        SharedPreferences.Editor se = getPrefs(ctx).edit();
        se.remove(uid);
        se.apply();
        Log.d(LOG_TAG, "Alarm with id " + uid + " removed from prefs");
    }

    // Receives the alarm broadcast
    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String NOTIFICATION_ID_FLAG = "NOTIFICATION_ID";

            final String alarmId = intent.getStringExtra(Const.EXTRA_ALARM_ID);

            final SharedPreferences sp = getPrefs(context);
            final int notificationId = sp.getInt(NOTIFICATION_ID_FLAG, 0);

            Realm.init(context);
            final Realm realm = Realm.getDefaultInstance();

            final Alarm alarm = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();

            final String ringtone = alarm.getChosenRingtone();
            final int vibrationMode = alarm.getChosenVibrationMode();

            final String stationName = alarm.getStationName();
            String stationId = alarm.getStationId();
            final int stationIndex = alarm.getStationArrayIndex();

            Log.d(LOG_TAG, "Retrieving realtime data for " + alarmId + " ...");
            RetrofitInfo.getRealtimeInfo().create(RetrofitInfo.RealtimeCalls.class).getRealtime(apikey, stationId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    String info = "";
                    if (!response.isSuccessful()) {
                        Log.e(LOG_TAG, "API response for " + alarmId + "unsuccessful");
                        info = "Error: Request failed - Code " + response.code();
                    } else {
                        Log.d(LOG_TAG, "API response for " + alarmId + " successful");
                        try {
                            JSONArray lines = new JSONObject(response.body().string())
                                    .getJSONObject("data")
                                    .getJSONArray("monitors")
                                    .getJSONObject(stationIndex)
                                    .getJSONArray("lines");
                            for (int i = 0; i < lines.length(); i++) {
                                JSONObject line = lines.getJSONObject(i);
                                String direction = line.getString("name") + " " + line.getString("towards"); // U6 ALTERLAA
                                JSONObject dep = line.getJSONObject("departures");
                                if (dep.length() == 0 || dep.getJSONArray("departure").length() == 0) {
                                    info += direction;
                                } else {
                                    JSONArray departures = dep.getJSONArray("departure");
                                    for (int j = 0; j < departures.length(); j++) {
                                        JSONObject departureTime = departures.getJSONObject(j).getJSONObject("departureTime");
                                        String timeString = (departureTime.has("timeReal")) ? departureTime.getString("timeReal") : departureTime.getString("timePlanned")+"(P)";
                                        String time = timeString.substring(11, 16); // exactly the "12:34" part
                                        String newline = (j != 0) ? "\n" : ""; // newline should not show up before the first line
                                        info += newline + direction + " " + time;
                                    }
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            info = e.getMessage();
                            Log.d(LOG_TAG, "IO/JSONException: " + e.getMessage());
                        }
                    }
                    Uri sound = (ringtone != null) ? Uri.parse(ringtone) : null;
                    Notification notification =
                            new Notification.Builder(context)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle(context.getString(R.string.app_name))
                                    .setContentText(stationName)
                                    .setStyle(new Notification.BigTextStyle()
                                            .setBigContentTitle(stationName)
                                            .bigText(info))
                                    .setSound(sound)
                                    .setVibrate(new long[]{0, Const.VIBRATION_DURATION[vibrationMode]})
                            .build();

                    notification.flags = Notification.FLAG_SHOW_LIGHTS;
                    notification.ledARGB = 0xFFFFFFFF;
                    notification.ledOnMS = 1000;
                    notification.ledOffMS = 1000;

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(notificationId, notification);

                    // update notification counter. this helps if a user sets alarms
                    // only a little apart (so notifications don't override each other)
                    sp.edit().putInt(NOTIFICATION_ID_FLAG, notificationId+1).apply();

                    switch (alarm.getAlarmMode()) {
                        case Const.ALARM_ONETIME:
                            removeAlarmFromPrefs(context, alarmId);
                            realm.beginTransaction();
                            realm.where(Alarm.class).equalTo("id", alarmId).findFirst().deleteFromRealm();
                            realm.commitTransaction();
                            break;
                        case Const.ALARM_RECURRING:
                            scheduleAlarm(context, alarm);
                            break;
                    }
                    Log.d(LOG_TAG, "Notification for " + alarmId + " was successful");

                    // Tell the MainActivity to refresh the list and the DialogEditActivity to kill the dialog
                    Intent i = new Intent(Const.INTENT_REFRESH_LIST);
                    context.sendBroadcast(i);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(LOG_TAG, "Network request failed: " + t.getMessage());

                    Uri sound = (ringtone != null) ? Uri.parse(ringtone) : null;

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification info = new Notification.Builder(context)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText("Error: " + t.getMessage())
                            .setSound(sound)
                            .setVibrate(new long[]{0, Const.VIBRATION_DURATION[vibrationMode]})
                            .build();

                    mNotificationManager.notify(notificationId, info);
                }
            });
        }
    }

    // Fires on "boot completed" to reschedule all alarms (because they get lost on reboot)
    public static class BootReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Received \"boot complete\": rescheduling all alarms");
            SharedPreferences sp = getPrefs(context);
            Map<String,?> keys = sp.getAll();

            Realm.init(context);
            Realm realm = Realm.getDefaultInstance();

            for (Map.Entry<String,?> entry : keys.entrySet()) {
                // reschedule all alarms
                Log.d(LOG_TAG, "Reschedule entry " + entry.getKey());
                Alarm alarm = realm.where(Alarm.class).equalTo("id", entry.getKey()).findFirst();
                if (alarm == null) {
                    Log.w(LOG_TAG, "Alarm with id " + entry.getKey() + " is null! Removing from prefs...");
                    removeAlarmFromPrefs(context, entry.getKey());
                } else {
                    scheduleAlarm(context, alarm);
                }
            }
        }
    }

}
