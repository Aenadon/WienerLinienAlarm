package aenadon.wienerlinienalarm.realtime;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;

public class RealtimeNotificationService extends IntentService {

    public RealtimeNotificationService() {
        super(RealtimeNotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            if (intent == null) {
                Log.e("Intent is null, terminating");
                return;
            }

            String alarmId = intent.getStringExtra(Keys.Extra.ALARM_ID);
            if (alarmId == null) {
                Log.e("Alarm ID was not passed to Service, terminating");
                return;
            }

            Alarm alarmOrNull = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
            if (alarmOrNull == null) {
                Log.e("No existing alarm for ID " + alarmId + ", terminating");
                return;
            }
            final Alarm alarm = realm.copyFromRealm(alarmOrNull);

            Log.d("Retrieving realtime data for " + alarmId + "…");
            new RealtimeProcessingTask(getApplicationContext(), alarm, intent.getExtras()).execute();
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
        }
    }
}
