package aenadon.wienerlinienalarm.activities;

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

import org.w3c.dom.Text;

import java.util.Calendar;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.utils.StringDisplay;

public class Pickers {

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private int[] chosenDate = null;

        private TextView viewToUse;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            viewToUse = (TextView) getActivity().findViewById(getArguments().getInt("viewToUse"));

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog d = new DatePickerDialog(getActivity(), this, year, month, day);
            d.getDatePicker().setMinDate(c.getTimeInMillis());
            if (chosenDate != null) d.getDatePicker().updateDate(chosenDate[0], chosenDate[1], chosenDate[2]);
            d.setTitle(null); // hides
            return d;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            chosenDate = new int[]{year, month, day};

            //((TextView) getActivity().findViewById(R.id.choose_date_text))
            viewToUse.setText(StringDisplay.getOnetimeDate(chosenDate[0], chosenDate[1], chosenDate[2]));
        }

        public int[] getPickedDate() {
            return chosenDate;
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        private int[] chosenTime = null;

        public int[] getPickedTime() {
            return chosenTime;
        }

        private TextView viewToUse;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            viewToUse = (TextView) getActivity().findViewById(getArguments().getInt("viewToUse"));

            // Create a new instance of TimePickerDialog and return it
            TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute, true);
            if (chosenTime != null) tpd.updateTime(chosenTime[0], chosenTime[1]);
            return tpd;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            chosenTime = new int[]{hourOfDay, minute};
            //((TextView) getActivity().findViewById(R.id.choose_time_text))
            viewToUse.setText(StringDisplay.getTime(chosenTime[0], chosenTime[1]));
        }
    }

    public static class DaysPicker {

        private String[] weekDayStrings;
        private boolean[] choice = new boolean[7];
        private boolean[] tempChoice = new boolean[7];

        public void show(final Context ctx, boolean[] previousChoice, final int viewResId) {
            if (previousChoice != null) {
                choice = previousChoice.clone();
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
                    .setMultiChoiceItems(weekDayStrings, choice, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            tempChoice[which] = isChecked;
                        }
                    })
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            choice = tempChoice.clone();
                            TextView viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
                            viewToUse.setText(StringDisplay.getRecurringDays(ctx, choice));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }

        public boolean[] getPickedDays() {
            return choice;
        }
    }

    public static class RingtonePicker {

        private String pickedRingtone;
        private TextView viewToUse;

        public void show(Context ctx, int viewResId) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            ((Activity)ctx).startActivityForResult(intent, Const.REQUEST_RINGTONE);
            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
        }

        public String getPickedRingtone() {
            return pickedRingtone;
        }

        public void setPickedRingtone(Context ctx, Intent data) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            pickedRingtone = (uri != null) ? uri.toString() : null;

            viewToUse.setText(StringDisplay.getRingtone(ctx, pickedRingtone));
        }
    }

    public static class VibrationPicker {

        private int pickedVibrationMode = Const.VIBRATION_NONE;
        private TextView viewToUse;

        public void show(final Context ctx, int viewResId) {
            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);

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

        public int getPickedVibrationMode() {
            return pickedVibrationMode;
        }

    }

    public static class StationPicker {

        // {stationName, stationDir, stationId, h.getArrayIndex()}
        private String pickedStationName;
        private String pickedStationDir;
        private String pickedStationId;
        private int pickedStationArrayIndex;

        private TextView viewToUse;

        public void show(Context ctx, int viewResId) {
            ((Activity)ctx).startActivityForResult(new Intent(ctx, StationPickerActivity.class), Const.REQUEST_STATION);
            viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
        }

        public void setPickedStation(Intent data) {
            String[] extras = data.getStringArrayExtra("stationInfo");

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

        public boolean stationWasSet() {
            return pickedStationId != null; // if one of them is not null, none are null (all are set at once)
        }
    }

}
