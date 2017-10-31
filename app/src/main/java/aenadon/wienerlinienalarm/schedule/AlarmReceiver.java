package aenadon.wienerlinienalarm.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import aenadon.wienerlinienalarm.BuildConfig;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;


public class AlarmReceiver extends BroadcastReceiver {

    private static String API_KEY = BuildConfig.API_KEY;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals(Keys.Intent.TRIGGER_ALARM)) {
            Log.e("Intent action not matching " + Keys.Intent.TRIGGER_ALARM + ", terminating");
            return;
        }

        String alarmId = intent.getStringExtra(Keys.Extra.ALARM_ID);
        if (alarmId == null) {
            Log.e("Alarm ID was not passed to Receiver, terminating");
            return;
        }
        Alarm alarm = getAlarmFromRealmAndDelete(alarmId);
        if (alarm == null) {
            Log.e("No existing alarm for ID " + alarmId + ", terminating");
            return;
        }

        Log.d("Retrieving realtime data for " + alarmId + " …");

        // TODO get realtime info and show notification
        goAsync();
    }

    private Alarm getAlarmFromRealmAndDelete(String alarmId) {
        Realm realm = Realm.getDefaultInstance();
        Alarm alarmInstanceDeletable = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
        if (alarmInstanceDeletable == null) {
            realm.close();
            return null;
        }
        Alarm alarm = realm.copyFromRealm(alarmInstanceDeletable);

        realm.beginTransaction();
        alarmInstanceDeletable.deleteFromRealm();
        realm.commitTransaction();

        realm.close();

        return alarm;
    }
}
