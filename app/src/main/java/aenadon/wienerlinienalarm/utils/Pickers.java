package aenadon.wienerlinienalarm.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.activities.StationPickerActivity;
import aenadon.wienerlinienalarm.models.Alarm;

public class Pickers {

    // Contains all pickers for the alarm settings.
    // They are for the picker activities

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private int[] chosenDate = null;
        private int viewResId;
        private TextView viewToUse;
        private boolean firstRun = true;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            viewResId = getArguments().getInt(Const.EXTRA_VIEW_TO_USE);
            viewToUse = (TextView) getActivity().findViewById(viewResId);
            int[] prevDate = getArguments().getIntArray(Const.EXTRA_PREV_DATE);

            Calendar c = Calendar.getInstance();
            int year, month, day;
            if (!firstRun) { // if he has already set a time, show him that one
                year = chosenDate[0];
                month = chosenDate[1];
                day = chosenDate[2];
            } else if (prevDate != null) {
                // if he hasn't set a time but there is one in the database, set that one
                year = prevDate[0];
                month = prevDate[1];
                day = prevDate[2];
            } else {
                // else use the current time as the default values for the picker
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog d = new DatePickerDialog(getActivity(), this, year, month, day);
            d.getDatePicker().setMinDate(c.getTimeInMillis());
            return d;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            chosenDate = new int[]{year, month, day};

            //((TextView) getActivity().findViewById(R.id.choose_date_text))
            viewToUse.setText(StringDisplay.getOnetimeDate(chosenDate));
            firstRun = false;
        }

        public int[] getChosenDate() {
            return chosenDate;
        }

        public boolean dateChanged(Alarm alarm) {
            // if date is NULL OR date is SAME, then nothing changed, return false
            // else something changed, return true
            return !(chosenDate == null || Arrays.equals(chosenDate, alarm.getOneTimeDateAsArray()));
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String chosenDateKey = "CHOSEN_TIME";
        private String firstRunKey = "FIRST_RUN";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putIntArray(chosenDateKey, chosenDate);
            b.putBoolean(firstRunKey, firstRun);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            chosenDate = b.getIntArray(chosenDateKey);
            firstRun = b.getBoolean(firstRunKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (chosenDate != null) viewToUse.setText(StringDisplay.getOnetimeDate(chosenDate));
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private int[] chosenTime = null;
        private int viewResId;
        private TextView viewToUse;
        private boolean firstRun = true;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            viewResId = getArguments().getInt(Const.EXTRA_VIEW_TO_USE);
            viewToUse = (TextView) getActivity().findViewById(viewResId);
            int[] prevTime = getArguments().getIntArray(Const.EXTRA_PREV_TIME);

            int hour, minute;
            if (!firstRun) { // if he has already set a time, show him that one
                hour = chosenTime[0];
                minute = chosenTime[1];
            } else if (prevTime != null) {
                // if he hasn't set a time but there is one in the database, set that one
                hour = prevTime[0];
                minute = prevTime[1];
            } else {
                // else use the current time as the default values for the picker
                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            chosenTime = new int[]{hourOfDay, minute};
            //((TextView) getActivity().findViewById(R.id.choose_time_text))
            viewToUse.setText(StringDisplay.getTime(chosenTime));
            firstRun = false;
        }

        public int[] getPickedTime() {
            return chosenTime;
        }

        public boolean timeChanged(Alarm alarm) {
            // if time is NULL OR time is SAME, then nothing changed, return false
            // else something changed, return true
            return !(chosenTime == null || Arrays.equals(chosenTime, alarm.getTimeAsArray()));
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String chosenTimeKey = "CHOSEN_TIME";
        private String firstRunKey = "FIRST_RUN";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putIntArray(chosenTimeKey, chosenTime);
            b.putBoolean(firstRunKey, firstRun);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            chosenTime = b.getIntArray(chosenTimeKey);
            firstRun = b.getBoolean(firstRunKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (chosenTime != null) viewToUse.setText(StringDisplay.getTime(chosenTime));
        }
    }

    public static class DaysPicker {

        private String[] weekDayStrings;
        private boolean[] choice;
        private boolean[] tempChoice = new boolean[7];
        private int viewResId;
        private TextView viewToUse;

        public void show(final Context ctx, boolean[] previousChoice, final int viewResId) {
            this.viewResId = viewResId;
            viewToUse = (TextView)((Activity)ctx).findViewById(this.viewResId);

            if (choice == null) {
                if (previousChoice != null) {
                    choice = previousChoice.clone();
                } else {
                    choice = new boolean[7];
                }
            }
            tempChoice = choice.clone();

            weekDayStrings = new String[]{
                    ctx.getString(R.string.monday),
                    ctx.getString(R.string.tuesday),
                    ctx.getString(R.string.wednesday),
                    ctx.getString(R.string.thursday),
                    ctx.getString(R.string.friday),
                    ctx.getString(R.string.saturday),
                    ctx.getString(R.string.sunday),
            };

            new AlertDialog.Builder(ctx)
                    .setTitle(ctx.getString(R.string.alarm_recurring_dialog_expl))
                    .setMultiChoiceItems(weekDayStrings, tempChoice, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            tempChoice[which] = isChecked;
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            choice = tempChoice.clone();
                            viewToUse.setText(StringDisplay.getRecurringDays(ctx, choice));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        public boolean[] getPickedDays() {
            return choice;
        }

        public boolean daysChanged(Alarm alarm) {
            // if days are NULL OR days are SAME, then nothing changed, return false
            // else something changed, return true
            return !(choice == null || Arrays.equals(choice, alarm.getRecurringChosenDays()));
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String choiceKey = "CHOICE";
        private String tempChoiceKey = "TEMP_CHOICE";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putBooleanArray(choiceKey, choice);
            b.putBooleanArray(tempChoiceKey, tempChoice);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            choice = b.getBooleanArray(choiceKey);
            tempChoice = b.getBooleanArray(tempChoiceKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (choice != null) viewToUse.setText(StringDisplay.getRecurringDays(ctx, choice));
        }
    }

    public static class RingtonePicker {

        private String pickedRingtone;
        private int viewResId;
        private TextView viewToUse;
        private boolean firstRun = true;

        public void show(Context ctx, String previousRingtone, int viewResId) {
            this.viewResId = viewResId;
            viewToUse = (TextView)((Activity)ctx).findViewById(this.viewResId);

            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);

            if (!firstRun) { // if user already saved a ringtone during the session, set it
                // he couldn't have done it before the first run
                if (pickedRingtone != null) intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(pickedRingtone));
            } else if (previousRingtone != null) { // if not, if user has had a ringtone set before, set that one
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(previousRingtone));
            }
            ((Activity)ctx).startActivityForResult(intent, Const.REQUEST_RINGTONE);
            firstRun = false;
        }

        public String getPickedRingtone() {
            return pickedRingtone;
        }

        public void setPickedRingtone(Context ctx, Intent data) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            pickedRingtone = (uri != null) ? uri.toString() : null;
            viewToUse.setText(StringDisplay.getRingtone(ctx, pickedRingtone));
        }

        public boolean ringtoneChanged(Alarm alarm) {
            // if ringtone is SAME, then nothing changed, return false
            // else something changed, return true
            return !(firstRun || // if user hasn't set anything yet
                    (pickedRingtone == null && alarm.getChosenRingtone() == null) || // or set it to null again
                    (pickedRingtone != null && pickedRingtone.equals(alarm.getChosenRingtone()))); // or set it to the same again
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String pickedRingtoneKey = "PICKED_RINGTONE_KEY";
        private String firstRunKey = "FIRST_RUN";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putString(pickedRingtoneKey, pickedRingtone);
            b.putBoolean(firstRunKey, firstRun);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            pickedRingtone = b.getString(pickedRingtoneKey);
            firstRun = b.getBoolean(firstRunKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (pickedRingtone != null) viewToUse.setText(StringDisplay.getRingtone(ctx, pickedRingtone));
        }
    }

    public static class VibrationPicker {

        private int pickedVibrationMode = -1;
        private int viewResId;
        private TextView viewToUse;

        public void show(final Context ctx, int viewResId) {
            this.viewResId = viewResId;
            viewToUse = (TextView)((Activity)ctx).findViewById(this.viewResId);

            final String[] vibrationModes = new String[]{
                    ctx.getString(Const.VIBRATION_STRINGS[Const.VIBRATION_NONE]),
                    ctx.getString(Const.VIBRATION_STRINGS[Const.VIBRATION_SHORT]),
                    ctx.getString(Const.VIBRATION_STRINGS[Const.VIBRATION_MEDIUM]),
                    ctx.getString(Const.VIBRATION_STRINGS[Const.VIBRATION_LONG])
            };

            final Vibrator v = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);

            new AlertDialog.Builder(ctx)
                    .setTitle(ctx.getString(R.string.alarm_choose_vibration_length))
                    .setItems(vibrationModes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (v.hasVibrator()) {
                                v.vibrate(Const.VIBRATION_DURATION[which]);
                            }
                            pickedVibrationMode = which;
                            viewToUse.setText(StringDisplay.getVibration(ctx, pickedVibrationMode));
                        }
                    })
                    .show();
        }

        public int getPickedVibrationMode() { // -1 == NOT SET. If asked, say "no vibration set"
            if (pickedVibrationMode == -1) return Const.VIBRATION_NONE;
            else return pickedVibrationMode;
        }

        public boolean vibrationChanged(Alarm alarm) {
            // if vibration is -1 OR vibration is SAME, then nothing changed, return false
            // else something changed, return true
            return !(pickedVibrationMode == -1 || pickedVibrationMode == alarm.getChosenVibrationMode());
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String pickedVibrationModeKey = "PICKED_VIBRATION_MODE_KEY";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putInt(pickedVibrationModeKey, pickedVibrationMode);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            pickedVibrationMode = b.getInt(pickedVibrationModeKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (pickedVibrationMode != -1) viewToUse.setText(StringDisplay.getVibration(ctx, pickedVibrationMode));
        }

    }

    public static class StationPicker {

        // {stationName, stationDir, stationId, h.getArrayIndex()}
        private String pickedStationName;
        private String pickedStationDir;
        private String pickedStationId;
        private int pickedStationArrayIndex;

        private int viewResId;
        private TextView viewToUse;

        public void show(Context ctx, int viewResId) {
            this.viewResId = viewResId;
            viewToUse = (TextView)((Activity)ctx).findViewById(this.viewResId);

            ((Activity)ctx).startActivityForResult(new Intent(ctx, StationPickerActivity.class), Const.REQUEST_STATION);
        }

        public void setPickedStation(Intent data) {
            String[] extras = data.getStringArrayExtra(Const.EXTRA_STATION_INFO);

            pickedStationName = extras[0];
            pickedStationDir = extras[1];
            pickedStationId = extras[2];
            pickedStationArrayIndex = Integer.parseInt(extras[3]);

            viewToUse.setText(StringDisplay.getStation(pickedStationName, pickedStationDir));
        }

        public String getPickedStationName() {
            return pickedStationName;
        }

        public String getPickedStationDir() {
            return pickedStationDir;
        }

        public String getPickedStationId() {
            return pickedStationId;
        }

        public int getPickedStationArrayIndex() {
            return pickedStationArrayIndex;
        }

        public String[] getStationInfoAsArray() {
            return new String[]{pickedStationName, pickedStationDir, pickedStationId, Integer.toString(pickedStationArrayIndex)};
        }

        public boolean stationWasSet() {
            return pickedStationId != null; // if one of them is not null, none are null (all are set at once)
        }

        public boolean stationChanged(Alarm alarm) {
            // if stationId is NULL OR stationId is SAME, then nothing changed, return false
            // else something changed, return true
            return !(pickedStationId == null || pickedStationId.equals(alarm.getStationId()));
        }

        private String viewResIdKey = "VIEW_RES_ID";
        private String pickedStationNameKey = "PICKED_STATION_NAME_KEY";
        private String pickedStationDirKey = "PICKED_STATION_DIR_KEY";
        private String pickedStationIdKey = "PICKED_STATION_ID_KEY";
        private String pickedStationArrayIndexKey = "PICKED_STATION_ARRAY_INDEX_KEY";

        public Bundle saveState() {
            Bundle b = new Bundle();
            b.putInt(viewResIdKey, viewResId);
            b.putString(pickedStationNameKey, pickedStationName);
            b.putString(pickedStationDirKey, pickedStationDir);
            b.putString(pickedStationIdKey, pickedStationId);
            b.putInt(pickedStationArrayIndexKey, pickedStationArrayIndex);
            return b;
        }

        public void restoreState(Context ctx, Bundle b) {
            viewResId = b.getInt(viewResIdKey);
            pickedStationName = b.getString(pickedStationNameKey);
            pickedStationDir = b.getString(pickedStationDirKey);
            pickedStationId = b.getString(pickedStationIdKey);
            pickedStationArrayIndex = b.getInt(pickedStationArrayIndexKey);

            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
            if (pickedStationName != null) viewToUse.setText(StringDisplay.getStation(pickedStationName, pickedStationDir));
        }
    }

}
