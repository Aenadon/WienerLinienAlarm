package aenadon.wienerlinienalarm.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.Keys;
import io.realm.Realm;
import trikita.log.Log;

public class BootRescheduler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("Intent action not matching " + Intent.ACTION_BOOT_COMPLETED + ", terminating");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(Keys.Prefs.KEY_SCHEDULED_ALARMS, Context.MODE_PRIVATE);
        Set<String> alarmIds = preferences.getAll().keySet();

        Realm realm = Realm.getDefaultInstance();

        List<Alarm> alarmsToReschedule = new ArrayList<>();
        List<String> invalidAlarmsToDelete = new ArrayList<>();

        for (String alarmId : alarmIds) {
            Alarm alarm = realm.where(Alarm.class).equalTo("id", alarmId).findFirst();
            if (alarm == null) {
                Log.w("Alarm with id " + alarmId + " is null! Removing from prefs...");
                invalidAlarmsToDelete.add(alarmId);
            } else {
                alarmsToReschedule.add(realm.copyFromRealm(alarm));
            }
        }
        realm.close();

        deleteInvalidAlarms(invalidAlarmsToDelete, preferences);

        BatchScheduler batchScheduler = new BatchScheduler(context, alarmsToReschedule);
        batchScheduler.scheduleAlarms();
    }

    private void deleteInvalidAlarms(List<String> invalidAlarms, SharedPreferences preferences) {
        SharedPreferences.Editor prefEditor = preferences.edit();
        for (String invalidAlarm : invalidAlarms) {
            prefEditor.remove(invalidAlarm);
        }
        prefEditor.apply();
    }
}
