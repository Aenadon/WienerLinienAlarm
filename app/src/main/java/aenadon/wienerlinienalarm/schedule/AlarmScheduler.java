package aenadon.wienerlinienalarm.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.enums.Weekday;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import trikita.log.Log;

public class AlarmScheduler {

    private Context ctx;
    protected Alarm alarm;
    private SharedPreferences prefs;
    private AlarmManager alarmManager;

    public AlarmScheduler(Context ctx, Alarm alarm) {
        this.ctx = ctx;
        this.alarm = alarm;
        this.prefs = ctx.getSharedPreferences(Keys.Prefs.FILE_SCHEDULED_ALARMS, Context.MODE_PRIVATE);
        this.alarmManager = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
    }

    public String scheduleAlarmAndReturnMessage() {
        ZonedDateTime alarmMoment = getNextAlarmMoment();
        PendingIntent alarmPendingIntent = buildPendingIntent();

        if (alarmManager == null || alarmMoment == null || alarmPendingIntent == null) {
            Log.e("Aborting alarm scheduling due to null value - " +
                    "\n\talarmManager null: " + (alarmManager == null) +
                    "\n\talarmTime null: " + (alarmMoment == null) +
                    "\n\talarmPendingIntent null: " + (alarmPendingIntent == null)
            );
            return ctx.getString(R.string.scheduling_error);
        }
        long alarmMillis = alarmMoment.toEpochSecond();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Window set for alarmMoment - 30sec to alarmMoment + 20sec.
            // It will probably never trigger exactly in that window
            // (due to Android's harsh wakeup restrictions) but very soon after
            alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    alarmMillis - 1000*30,
                    1000*30,
                    alarmPendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmMillis,
                    alarmPendingIntent
            );
        }

        addAlarmToPrefs();

        String nextAlarmMessage = StringDisplay.getAlarmMoment(alarmMoment);
        Log.d("Scheduled alarm with id: " + alarm.getId() + " at " + nextAlarmMessage);

        return ctx.getString(R.string.alarm_next_ring, nextAlarmMessage);
    }

    private ZonedDateTime getNextAlarmMoment() {
        // as WienerLinien only operate in Austria, any other time zone does not make sense
        ZoneId europeanCentralTime = ZoneId.of("Europe/Vienna");
        if (alarm.getAlarmType() == AlarmType.ONETIME) {
            LocalDateTime alarmDateTime = LocalDateTime.of(alarm.getOnetimeAlarmDate(), alarm.getAlarmTime());
            return alarmDateTime.atZone(europeanCentralTime);
        } else {
            LocalTime alarmTime = alarm.getAlarmTime();
            boolean todaysTimeIsInAWeek = alarmTime.isBefore(LocalTime.now());

            DayOfWeek currentDay = LocalDate.now().getDayOfWeek();

            Set<Weekday> chosenDaysSet = alarm.getRecurringChosenDays();

            List<Weekday> allWeekdays = new ArrayList<>(Weekday.getAllWeekdaysList());
            // today => index 0, *-1 to rotate from beginning to end
            Collections.rotate(allWeekdays, currentDay.ordinal() * -1);

            int offset = -1;
            for (int i = 0; i < allWeekdays.size(); i++) {
                Weekday w = allWeekdays.get(i);
                if (chosenDaysSet.contains(w)) {
                    offset = i;
                    if (i == 0 && todaysTimeIsInAWeek) {
                        offset = 7;
                        continue; // maybe there's a closer day than 7
                    }
                    break;
                }
            }
            if (offset == -1) {
                Log.e("Error at calculating day offset");
                return null;
            }

            LocalDate todayPlusOffset = LocalDate.now().plus(offset, ChronoUnit.DAYS);
            LocalDateTime dateTimePlusOffset = LocalDateTime.of(todayPlusOffset, alarmTime);
            return dateTimePlusOffset.atZone(europeanCentralTime);
        }
    }

    public void cancelAlarmIfScheduled() {
        PendingIntent pendingIntent = buildPendingIntent();
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);

        removeAlarmFromPrefs();

        Log.d("Canceled alarm with id: " + alarm.getId());
    }

    private void addAlarmToPrefs() {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putBoolean(alarm.getId(), true);
        prefEditor.apply();
        Log.v("Alarm with id " + alarm.getId() + " added to prefs");
    }

    private void removeAlarmFromPrefs() {
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.remove(alarm.getId());
        prefEditor.apply();
        Log.v("Alarm with id " + alarm.getId() + " removed from prefs");
    }

    private PendingIntent buildPendingIntent() {
        Intent i = new Intent(ctx, AlarmReceiver.class);
        i.putExtra(Keys.Extra.ALARM_ID, alarm.getId());
        i.setAction(Keys.Intent.TRIGGER_ALARM);
        return PendingIntent.getBroadcast(ctx, getNotificationId(), i, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private int getNotificationId() {
        SharedPreferences notificationIdPrefs = ctx.getSharedPreferences(Keys.Prefs.FILE_AUTOINCREMENT_ID, Context.MODE_PRIVATE);
        // should never need more than 100 notifications at once
        int notificationId = (notificationIdPrefs.getInt(Keys.Prefs.KEY_AUTOINCREMENT_ID, -1) + 1) % 100;
        notificationIdPrefs.edit().putInt(Keys.Prefs.KEY_AUTOINCREMENT_ID, notificationId).apply();
        return notificationId;
    }
}
