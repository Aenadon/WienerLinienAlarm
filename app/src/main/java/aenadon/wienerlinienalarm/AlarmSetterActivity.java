package aenadon.wienerlinienalarm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

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

        new GetApiFiles(this).execute();
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

        LinearLayout chooseDateContainer = (LinearLayout) findViewById(R.id.choose_date_container);
        LinearLayout chooseDaysContainer = (LinearLayout) findViewById(R.id.choose_days_container);
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

                TextView t = (TextView) findViewById(R.id.choose_date_text);
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

                                                                                 TextView t = (TextView) findViewById(R.id.choose_time_text);
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
                .setPositiveButton(R.string.ok, dayDialogListener(tempChoices))
                .setNegativeButton(R.string.cancel, dayDialogListener(tempChoices))
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

                        String selection = (noDaysChosen()) ?
                                getString(R.string.alarm_no_days_set) :
                                getResources().getQuantityString(R.plurals.days_chosen, selectedDays, selectedDays);

                        TextView t = (TextView) findViewById(R.id.choose_days_text);
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

    private boolean noDaysChosen() {
        return !(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4] || chosenDays[5] || chosenDays[6]);
    }

    private void pickStation() {
        startActivityForResult(new Intent(this, StationPicker.class), 0);
    }

    class GetApiFiles extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog warten;
        Context mContext;

        GetApiFiles(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            warten = new ProgressDialog(mContext);
            warten.setCancelable(false);
            warten.setIndeterminate(true);
            warten.setMessage("Stationsinfo wird heruntergeladen..."); // TODO R STRING
            warten.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Response<ResponseBody> versionResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getVersionCSV().execute(); // check file version
                String versionResponseString = versionResponse.body().string();

                if (!versionResponse.isSuccessful()) return false;
                File csv = new File(mContext.getFilesDir(), C.CSV_FILENAME);
                String csvString = C.getCSVfromFile(mContext);
                if (csv.exists() && csvString != null) {
                    String x = csvString.split(C.CSV_FILE_SEPARATOR)[C.CSV_PART_VERSION];
                    if (x.equals(versionResponseString))
                        return true; // if we already have the latest version, skip the redownload
                }

                Response<ResponseBody> haltestellenResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getHaltestellenCSV().execute();
                Response<ResponseBody> steigResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getSteigeCSV().execute();

                if (!haltestellenResponse.isSuccessful() || !steigResponse.isSuccessful()) {
                    throw new IOException("At least one server response not successful " +
                            "(" + haltestellenResponse.code() + "/" + steigResponse.code() + ")"); // [...] (403/403)
                } else {
                    if (csv.exists()) csv.delete();
                    String combined =
                            versionResponseString      // last update date
                                    + C.CSV_FILE_SEPARATOR +             // separator
                                    haltestellenResponse.body().string() // haltestellen CSV
                                    + C.CSV_FILE_SEPARATOR +             // separator
                                    steigResponse.body().string();       // steige CSV

                    FileOutputStream fos = new FileOutputStream(csv);
                    fos.write(combined.getBytes());
                    fos.close();
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            warten.dismiss();

            if (!success) {
                AlertDialogs.serverNotAvailable(mContext);
                findViewById(R.id.choose_station_button).setEnabled(false); // disable station picker
            }
        }
    }
}
