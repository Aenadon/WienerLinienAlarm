package aenadon.wienerlinienalarm;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AlarmSetterActivity extends AppCompatActivity {

    private static final int ALARM_ONETIME = 0;
    private static final int ALARM_RECURRING = 1;
    private int ALARM_MODE = ALARM_ONETIME;

    private boolean dateIsToday = false; // flag for checking if today is selected if time has passed
    private int[] chosenDate; // Chosen Date
    private int[] chosenTime; // arr[0] = hours; arr[1] = minutes;

    private Date selectedAlarmTime;    // exact timestamp as date

    private boolean[] chosenDays = new boolean[7]; // true,true,false,false,false,false,true ==> Monday, Tuesday, Sunday

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.radio_frequency_one_time:
                adjustToFrequency(ALARM_ONETIME);
                break;
            case R.id.radio_frequency_recurring:
                adjustToFrequency(ALARM_RECURRING);
                break;
            case R.id.choose_date_button:
                pickDate();
                break;
            case R.id.choose_time_button:
                pickTime();
                break;
            case R.id.choose_days_button:
                pickDays();
                break;
            case R.id.choose_station_button:
                pickStation();
                break;
        }
    }

    private void adjustToFrequency(int setTo) {
        if (ALARM_MODE == setTo) return; // if nothing changed, do nothing

        LinearLayout chooseDateContainer = (LinearLayout) findViewById (R.id.choose_date_container);
        LinearLayout chooseDaysContainer = (LinearLayout) findViewById (R.id.choose_days_container);
        // LinearLayout chooseTimeContainer; --> always on screen!

        switch (setTo) {
            case ALARM_ONETIME:   // hide the days+time chooser and show the date chooser
                chooseDaysContainer.setVisibility(View.GONE);
                // -- //
                chooseDateContainer.setVisibility(View.VISIBLE);
                break;
            case ALARM_RECURRING: // hide the date chooser and show the days+time chooser
                chooseDateContainer.setVisibility(View.GONE);
                // -- //
                chooseDaysContainer.setVisibility(View.VISIBLE);
                break;
        }
        ALARM_MODE = setTo; // in the end, set the mode as current mode
    }

    private void pickDate() {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cal = GregorianCalendar.getInstance();
                cal.set(year, monthOfYear, dayOfMonth);
                Date chosenDateAtMidnight = cal.getTime();
                String formattedDate = DateFormat.getDateInstance().format(chosenDateAtMidnight);
                Calendar nowAfterChoosing = Calendar.getInstance(); // we recalculate the current time because it has changed since the picker opened
                dateIsToday = (chosenDateAtMidnight.before(nowAfterChoosing.getTime())); // chosenDateAtMidnight is always 00:00:00:0000

                chosenDate = new int[]{year, monthOfYear, dayOfMonth};

                TextView t = (TextView) findViewById (R.id.choose_date_text);
                t.setText(formattedDate);
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.dismissOnPause(true);
        datePickerDialog.setMinDate(now);
        datePickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    private void pickTime() {
        Calendar now = GregorianCalendar.getInstance();

        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                chosenTime = new int[]{hourOfDay, minute};

                TextView t = (TextView) findViewById (R.id.choose_time_text);
                t.setText(hourOfDay + ":" + String.format(Locale.ENGLISH, "%02d", minute));
                // TODO check for past times!!!
            }
        },
                now.get(Calendar.HOUR_OF_DAY), // sets the clock to now
                now.get(Calendar.MINUTE),      // sets the clock to now
                true); // 24 hour format. No hassle with AM/PM

        timePickerDialog.dismissOnPause(true);
        timePickerDialog.show(getFragmentManager(), "TimePickerDialog");
    }

    private void pickDays() {

        final String[] weekDayStrings = new String[]{
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday),
                getString(R.string.sunday),
        };

        final boolean[] tempChoices = chosenDays.clone();

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alarm_recurring_dialog_expl))
                .setMultiChoiceItems(weekDayStrings, tempChoices, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        tempChoices[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.ok, dayDialogListener(tempChoices)) // TODO STRING
                .setNegativeButton(R.string.cancel, dayDialogListener(tempChoices)) // TODO STRING
                .show();
    }

    private DialogInterface.OnClickListener dayDialogListener(final boolean[] tempChoices) {
        final String[] daysShort = new String[]{
                getString(R.string.monday_short),
                getString(R.string.tuesday_short),
                getString(R.string.wednesday_short),
                getString(R.string.thursday_short),
                getString(R.string.friday_short),
                getString(R.string.saturday_short),
                getString(R.string.sunday_short),
        };


        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        chosenDays = tempChoices.clone(); // assign our tempChoices to our "persistent" choices

                        int selectedDays = 0;
                        for (int i = 0; i < 7; i++) {
                            if (chosenDays[i]) selectedDays++;
                        }

/*                        String selection;
                        if (weekDaysSelected() && weekendSelected()) {
                            selection = getString(R.string.everyday);
                        } else if (weekDaysSelected()) {
                            selection = getString(R.string.weekdays);
                            for (int i = 5; i < 7; i++) {
                                if (chosenDays[i]) selection += "," + daysShort[i];
                            }
                        } else if (weekendSelected()) {
                            selection = "";
                            for (int i = 0; i < 5; i++) {
                                if (chosenDays[i]) selection += daysShort[i] + ",";
                            }
                            selection += getString(R.string.weekend);

                        } else {
                            ArrayList<String> resDays = new ArrayList<>();
                            for (int i = 0; i < chosenDays.length; i++) {
                                if (chosenDays[i]) resDays.add(daysShort[i]);
                            }
                            selection = TextUtils.join(",", resDays.toArray());
                        }*/

                        String selection = (noDaysChosen()) ?
                                getString(R.string.alarm_no_days_set) :
                                getResources().getQuantityString(R.plurals.days_chosen, selectedDays, selectedDays);

                        TextView t = (TextView) findViewById (R.id.choose_days_text);
                        t.setText(selection);
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
    }

    private boolean weekDaysSelected() {
        return chosenDays[0] && chosenDays[1] && chosenDays[2] && chosenDays[3] && chosenDays[4];
    }
    private boolean weekendSelected() {
        return chosenDays[5] && chosenDays[6];
    }
    private boolean noDaysChosen() {
        return !(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4] || chosenDays[5] || chosenDays[6]);
    }

    private void pickStation() {
        Toast.makeText(this, "To be implemented", Toast.LENGTH_LONG).show();
    }
}
