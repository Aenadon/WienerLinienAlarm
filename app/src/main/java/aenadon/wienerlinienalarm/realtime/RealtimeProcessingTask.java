package aenadon.wienerlinienalarm.realtime;

import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.IOException;
import java.lang.ref.WeakReference;
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
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import trikita.log.Log;

class RealtimeProcessingTask extends AsyncTask<Void, Void, Notification> {

    private static final String API_KEY = BuildConfig.API_KEY;

    private WeakReference<Context> weakCtx;
    private ApiProvider.RealtimeApi realtimeApi;
    private Alarm alarm;
    private int retries;

    private NotificationCompat.Builder notificationBuilder;

    RealtimeProcessingTask(Context ctx, Alarm alarm, int retries) {
        this.weakCtx = new WeakReference<>(ctx);
        realtimeApi = ApiProvider.getRealtimeApi();
        this.alarm = alarm;

        this.retries = retries;

        notificationBuilder = new NotificationCompat.Builder(ctx, "chid");

        String ringtoneUriString = alarm.getPickedRingtone();
        Uri ringtoneUri = (ringtoneUriString != null) ? Uri.parse(ringtoneUriString) : null;
        notificationBuilder.setContentTitle(ctx.getString(R.string.friday))
                .setVibrate(new long[]{0, alarm.getPickedVibrationMode().getDuration()})
                .setLights(0x00FF00, 500, 500)
                .setSound(ringtoneUri);
    }

    @Override
    protected Notification doInBackground(Void... voids) {
        try {
            Response<RealtimeData> response = call(realtimeApi.getRealtime(API_KEY, alarm.getRbl()));
            RealtimeData data = response.body();
            List<SteigDeparture> steigDepartures = SteigDeparture.getDepartures(data);

            // TODO build notification text

            if (alarm.getAlarmType() == AlarmType.ONETIME) {
                deleteAlarm(alarm);
            } else {
                rescheduleRecurringAlarm(alarm);
            }

            return notificationBuilder.build();
        } catch (NetworkClientException | NetworkServerException e) {
            Log.e("Realtime request error", e);
            if (shouldRetry(retries)) {
                retryInOneMinute(alarm, retries);
                notificationBuilder.setContentText("error Retrying...."); // TODO use message
            } else {
                notificationBuilder.setContentText("error Not retrying"); // TODO use message
            }
            return notificationBuilder.build();
        }
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

    private boolean shouldRetry(int retries) {
        return retries <= 2;
    }

    private void retryInOneMinute(Alarm alarm, int retries) {
        if (retries <= 2) {
            AlarmScheduler scheduler = new AlarmScheduler(weakCtx.get(), alarm);
            ZonedDateTime oneMinuteLater = ZonedDateTime.now().plus(1, ChronoUnit.MINUTES);

            scheduler.scheduleAlarm(oneMinuteLater, retries + 1);
        }
    }

    private void rescheduleRecurringAlarm(Alarm alarm) {
        AlarmScheduler scheduler = new AlarmScheduler(weakCtx.get(), alarm);
        scheduler.scheduleAlarm();
    }

    private void deleteAlarm(Alarm alarm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        alarm.deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }
}
