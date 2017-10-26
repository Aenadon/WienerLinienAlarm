package aenadon.wienerlinienalarm.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.Weekday;
import aenadon.wienerlinienalarm.models.alarm.Alarm;

public class StringDisplay {

    private static SimpleDateFormat dateFormat =
            (SimpleDateFormat)SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
    private static SimpleDateFormat timeFormat =
            (SimpleDateFormat)SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
    private static SimpleDateFormat dateTimeFormat =
            (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    public static String getDate(Context ctx, Alarm alarm) {
        if (alarm.getAlarmType() == AlarmType.ONETIME) {
            return getOnetimeDate(alarm.getOnetimeAlarmDate());
        } else {
            return getRecurringDays(ctx, alarm.getRecurringChosenDays());
        }
    }

    public static String getOnetimeDate(LocalDate date) {
        return formatTimeObject(date, dateFormat);
    }

    public static String getTime(LocalTime time) {
        return formatTimeObject(time, timeFormat);
    }

    public static String getAlarmMoment(ZonedDateTime nextAlarm) {
        return formatTimeObject(nextAlarm, dateTimeFormat);
    }

    private static String formatTimeObject(TemporalAccessor timeObject, SimpleDateFormat format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format.toLocalizedPattern());
        return formatter.format(timeObject);
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

    public static String getRingtone(Context ctx, String chosenRingtone) {
        if (chosenRingtone == null) {
            return ctx.getString(R.string.alarm_none);
        }
        Uri uri = Uri.parse(chosenRingtone);
        Ringtone ringtone = RingtoneManager.getRingtone(ctx, uri);
        return ringtone.getTitle(ctx);

    }
}
