package aenadon.wienerlinienalarm.realtime;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;

public class RealtimeNotificationService extends IntentService {

    private Realm realm;

    public RealtimeNotificationService() {
        super(RealtimeNotificationService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            Log.e("Intent is null, terminating");
            return;
        }

        int retries = intent.getIntExtra(Keys.Extra.RETRIES_COUNT, 0);
        String alarmId = intent.getStringExtra(Keys.Extra.ALARM_ID);
        if (alarmId == null) {
            Log.e("Alarm ID was not passed to Service, terminating");
            return;
        }

        final Alarm alarm = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
        if (alarm == null) {
            Log.e("No existing alarm for ID " + alarmId + ", terminating");
            return;
        }
        Log.d("Retrieving realtime data for " + alarmId + "…");
        new RealtimeProcessingTask(getApplicationContext(), alarm, retries).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
