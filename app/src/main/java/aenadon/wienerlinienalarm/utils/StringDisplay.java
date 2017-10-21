package aenadon.wienerlinienalarm.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.Weekday;
import aenadon.wienerlinienalarm.models.alarm.Alarm;

public class StringDisplay {

    // Used for parsing the display strings on Alarm set/edit

    public static String getDate(Context ctx, Alarm alarm) {
        if (alarm.getAlarmType() == AlarmType.ONETIME) {
            return getOnetimeDate(alarm.getOnetimeDate());
        } else {
            return getRecurringDays(ctx, alarm.getRecurringDays());
        }
    }

    public static String getOnetimeDate(LocalDate date) {
        SimpleDateFormat dateFormat = (SimpleDateFormat)SimpleDateFormat.getDateInstance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat.toLocalizedPattern());
        return formatter.format(date);
    }

    public static String getRecurringDays(Context ctx, Set<Weekday> chosenDays) {
        if (chosenDays.isEmpty()) {
            return ctx.getString(R.string.alarm_no_days_set);
        } else if (Weekday.weekdaysOnlySelected(chosenDays)) {
            return ctx.getString(R.string.weekdays);
        } else if (Weekday.weekendOnlySelected(chosenDays)){
            return ctx.getString(R.string.weekends);
        } else if (chosenDays.size() == 7) {
            return ctx.getString(R.string.everyday);
        } else {
            return ctx.getResources().getQuantityString(R.plurals.days_selected, chosenDays.size(), chosenDays.size());
        }
    }

    private static String getTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    public static String getTime(int[] time) {
        return getTime(time[0], time[1]);
    }

    public static String getTime(LocalTime time) {
        return getTime(time.getHour(), time.getMinute());
    }

    public static String getRingtone(Context ctx, String chosenRingtone) {
        if (chosenRingtone == null) {
            return ctx.getString(R.string.alarm_none);
        }
        Uri uri = Uri.parse(chosenRingtone);
        Ringtone ringtone = RingtoneManager.getRingtone(ctx, uri);
        return ringtone.getTitle(ctx);

    }
}
