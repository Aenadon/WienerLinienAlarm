package aenadon.wienerlinienalarm.utils;

import android.content.Context;

import aenadon.wienerlinienalarm.models.alarm.LegacyAlarm;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmService {

    // This class contains some methods for working with Realm

    public static RealmResults<LegacyAlarm> getAlarms(Context c, int type) {
        Realm realm = Realm.getDefaultInstance();

        String[] sortAfter;
        Sort[] sortOrder;

        switch (type) {
            case Const.ALARM_ONETIME:
                sortAfter = new String[]{
                        "oneTimeAlarmYear",
                        "oneTimeAlarmMonth",
                        "oneTimeAlarmDay",
                        "alarmHour",
                        "alarmMinute"
                };
                sortOrder = new Sort[]{
                        Sort.ASCENDING,
                        Sort.ASCENDING,
                        Sort.ASCENDING,
                        Sort.ASCENDING,
                        Sort.ASCENDING
                };
                break;
            case Const.ALARM_RECURRING:
                sortAfter = new String[]{
                        "alarmHour",
                        "alarmMinute",
                        "recurringChosenDays"
                };
                sortOrder = new Sort[]{
                        Sort.ASCENDING,
                        Sort.ASCENDING,
                        Sort.ASCENDING
                };
                break;
            default:
                throw new Error("Non-existent alarm mode");
        }

        return realm.where(LegacyAlarm.class).equalTo("alarmMode", type).findAllSorted(sortAfter, sortOrder);
    }

}
