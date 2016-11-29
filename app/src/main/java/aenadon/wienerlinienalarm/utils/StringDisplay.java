package aenadon.wienerlinienalarm.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import aenadon.wienerlinienalarm.R;

public class StringDisplay {

    // Used for parsing the display strings on Alarm set/edit

    // Gets the onetime date in the phone's default locale
    public static String getOnetimeDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return DateFormat.getDateInstance().format(cal.getTime());
    }

    public static String getOnetimeDate(int[] date) {
        return getOnetimeDate(date[0], date[1], date[2]);
    }

    public static String getRecurringDays(Context c, boolean[] chosenDays) {
        String selection;
        if (!(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4] || chosenDays[5] || chosenDays[6])) {
            selection = c.getString(R.string.alarm_no_days_set);  // then say "no days selected"
        } else if (!(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4]) && (chosenDays[5] && chosenDays[6])) {
            selection = c.getString(R.string.weekends);
        } else if ((chosenDays[0] && chosenDays[1] && chosenDays[2] && chosenDays[3] && chosenDays[4]) && !(chosenDays[5] || chosenDays[6])) {
            selection = c.getString(R.string.weekdays);
        } else if (chosenDays[0] && chosenDays[1] && chosenDays[2] && chosenDays[3] && chosenDays[4] && chosenDays[5] && chosenDays[6]) {
            selection = c.getString(R.string.everyday);
        } else {
            int selectedDays = 0;
            for (int i = 0; i < 7; i++) {
                if (chosenDays[i]) selectedDays++;
            }
            selection = c.getResources().getQuantityString(R.plurals.days_chosen, selectedDays, selectedDays); // else show the count of days chosen
        }
        return selection;
    }

    public static String getTime(int hour, int minute) {
        return String.format(Locale.ENGLISH, "%02d:%02d", hour, minute);
    }

    public static String getTime(int[] time) {
        return getTime(time[0], time[1]);
    }

    public static String getRingtone(Context c, String chosenRingtone) {
        if (chosenRingtone == null) {
            return c.getString(R.string.alarm_none);
        }
        Uri uri = Uri.parse(chosenRingtone);
        Ringtone ringtone = RingtoneManager.getRingtone(c, uri);
        return ringtone.getTitle(c);

    }

    public static String getVibration(Context c, int vibrationMode) {
        return c.getString(Const.VIBRATION_STRINGS[vibrationMode]);
    }

    public static String getStation(String station, String direction) {
        return station + "\n" + direction;
    }
}
