package aenadon.wienerlinienalarm.utils;

import android.content.Context;

import aenadon.wienerlinienalarm.models.Alarm;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmUtils {

    // This class contains some methods for working with Realm

    public static RealmResults<Alarm> getAlarms(Context c, int type) {
        Realm.init(c);
        Realm realm = Realm.getDefaultInstance();

        String[] sortAfter;
        Sort[] sortOrder;

        if (type == Const.ALARM_ONETIME) {
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
        } else {
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
        }

        return realm.where(Alarm.class).equalTo("alarmMode", type).findAllSorted(sortAfter, sortOrder);
    }

}