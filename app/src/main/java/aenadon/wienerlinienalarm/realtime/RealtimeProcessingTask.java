package aenadon.wienerlinienalarm.realtime;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.exceptions.NetworkClientException;
import aenadon.wienerlinienalarm.exceptions.NetworkServerException;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.models.realtime.SteigDeparture;
import aenadon.wienerlinienalarm.models.realtime.json_model.RealtimeData;
import aenadon.wienerlinienalarm.schedule.AlarmScheduler;
import aenadon.wienerlinienalarm.utils.ApiProvider;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import trikita.log.Log;

class RealtimeProcessingTask extends AsyncTask<Void, Void, Notification> {

    private static final String API_KEY = BuildConfig.API_KEY;

    private ApiProvider.RealtimeApi realtimeApi;
    private Alarm alarm;

    private int retries;
    private int notificationId;

    private AlarmScheduler alarmScheduler;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private final String realtimeErrorServerRetryMessage;
    private final String realtimeErrorServerMessage;
    private final String realtimeErrorClientRetryMessage;
    private final String realtimeErrorClientMessage;

    RealtimeProcessingTask(Context ctx, Alarm alarm, Bundle extraBundle) {
        realtimeApi = ApiProvider.getRealtimeApi();
        this.alarm = alarm;

        this.retries = extraBundle.getInt(Keys.Extra.RETRIES_COUNT, 0);
        this.notificationId = extraBundle.getInt(Keys.Extra.NOTIFICATION_ID, 0);

        this.alarmScheduler = new AlarmScheduler(ctx, alarm);
        this.notificationBuilder = new NotificationCompat.Builder(ctx, ctx.getString(R.string.notification_channel_realtime));

        this.realtimeErrorServerRetryMessage = ctx.getString(R.string.realtime_error_server_retry);
        this.realtimeErrorServerMessage = ctx.getString(R.string.realtime_error_server);
        this.realtimeErrorClientRetryMessage = ctx.getString(R.string.realtime_error_client_retry);
        this.realtimeErrorClientMessage = ctx.getString(R.string.realtime_error_client);

        String ringtoneUriString = alarm.getPickedRingtone();
        Uri ringtoneUri = (ringtoneUriString != null) ? Uri.parse(ringtoneUriString) : null;
        notificationBuilder.setContentTitle(alarm.getLineName() + " " + alarm.getStationName())
                .setVibrate(new long[]{0, alarm.getPickedVibrationMode().getDuration()})
                .setLights(0x00FF00, 500, 500)
                .setSound(ringtoneUri)
                .setSmallIcon(R.drawable.ic_tram);

        notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected Notification doInBackground(Void... voids) {
        try {
            Response<RealtimeData> response = call(realtimeApi.getRealtime(API_KEY, alarm.getRbl()));
            RealtimeData data = response.body();
            List<SteigDeparture> steigDepartures = SteigDeparture.getDepartures(data, alarm.getLine());

            List<String> departureTimes = new ArrayList<>();
            for (int i = 0; i < steigDepartures.size(); i++) {
                SteigDeparture departure = steigDepartures.get(i);

                ZonedDateTime departureTime;
                boolean hasRealtime = departure.isRealtimeSupported() && departure.getRealtimeDeparture() != null;
                if (hasRealtime) {
                    departureTime = departure.getRealtimeDeparture();
                } else {
                    departureTime = departure.getPlannedDeparture();
                }

                String towards = departure.getTowards();
                String formattedDepartureTime = StringDisplay.formatZonedDateTimeAsTime(departureTime);
                String plannedTimeIndicator = hasRealtime ? "" : "*";

                departureTimes.add(towards + "\t" + formattedDepartureTime + plannedTimeIndicator);
            }
            String notificationContentText = TextUtils.join("\n", departureTimes);
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContentText));

            if (alarm.getAlarmType() == AlarmType.ONETIME) {
                deleteAlarm(alarm);
            } else {
                alarmScheduler.rescheduleAlarmAtPlannedTime();
            }

            return notificationBuilder.build();
        } catch (NetworkServerException e) {
            Log.e("Realtime server error", e);
            if (shouldRetry()) {
                retryInOneMinute();
                notificationBuilder.setContentText(realtimeErrorServerRetryMessage);
            } else {
                notificationBuilder.setContentText(realtimeErrorServerMessage);
            }
            return notificationBuilder.build();
        } catch (NetworkClientException e) {
            Log.e("Realtime client error", e);
            if (shouldRetry()) {
                retryInOneMinute();
                notificationBuilder.setContentText(realtimeErrorClientRetryMessage);
            } else {
                notificationBuilder.setContentText(realtimeErrorClientMessage);
            }
            return notificationBuilder.build();
        }
    }

    private boolean shouldRetry() {
        return retries <= 2;
    }

    private void retryInOneMinute() {
        ZonedDateTime oneMinuteLater = ZonedDateTime.now().plus(1, ChronoUnit.MINUTES);
        alarmScheduler.rescheduleAlarm(oneMinuteLater, retries + 1);
    }

    @Override
    protected void onPostExecute(Notification notification) {
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        } else {
            Log.e("NotificationManager is null! No notification was displayed");
            return;
        }
        super.onPostExecute(notification);
    }

    private Response<RealtimeData> call(Call<RealtimeData> networkCall) throws NetworkClientException, NetworkServerException {
        Response<RealtimeData> response;
        try {
            response = networkCall.execute();
            if (!response.isSuccessful()) {
                throw new NetworkServerException(response.code());
            }
        } catch (IOException e) {
            throw new NetworkClientException(e.getMessage(), e);
        }
        return response;
    }

    private void deleteAlarm(Alarm alarm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Alarm managedAlarm = realm.where(Alarm.class).equalTo("id", alarm.getId()).findFirst();
        if (managedAlarm != null) {
            managedAlarm.deleteFromRealm();
        }

        realm.commitTransaction();
        realm.close();

        alarmScheduler.removeAlarmFromPrefs();
    }
}
