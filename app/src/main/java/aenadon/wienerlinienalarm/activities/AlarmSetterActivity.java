package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.C;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.RetrofitInfo;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class AlarmSetterActivity extends AppCompatActivity {

    private int ALARM_MODE = C.ALARM_ONETIME;

    Vibrator v;

    private boolean[] chosenDays = new boolean[7]; // true,true,false,false,false,false,true ==> Monday, Tuesday, Sunday

    private String chosenRingtone = null;               // standard: no sound
    private int chosenVibratorMode = C.VIBRATION_NONE;  // standard: no vibration

    private String[] pickedStationData = null; // {stationName, stationDir, stationId, h.getArrayIndex()}

    private Pickers.DatePickerFragment datePicker;
    private Pickers.TimePickerFragment timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        // if device has no vibrator, hide the vibration choice
        v = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        if (!v.hasVibrator()) {
            findViewById(R.id.choose_vibration_container).setVisibility(View.GONE);
        }

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Realm.init(AlarmSetterActivity.this);

        new GetApiFiles(AlarmSetterActivity.this).execute(); // get CSV files/check for updates on them
    }

    // handling all the click events from the view
    public void onClickHandler(View view) {
        switch (view.getId()) {
            case R.id.radio_frequency_one_time:
                pickAlarmFrequency(C.ALARM_ONETIME);
                break;
            case R.id.radio_frequency_recurring:
                pickAlarmFrequency(C.ALARM_RECURRING);
                break;
            case R.id.choose_date_button:
            case R.id.choose_date_text:
                pickDate();
                break;
            case R.id.choose_time_button:
            case R.id.choose_time_text:
                pickTime();
                break;
            case R.id.choose_days_button:
            case R.id.choose_days_text:
                pickDays();
                break;
            case R.id.choose_station_button:
            case R.id.choose_station_text:
                pickStation();
                break;
            case R.id.choose_ringtone_button:
            case R.id.choose_ringtone_text:
                pickRingtone();
                break;
            case R.id.choose_vibration_button:
            case R.id.choose_vibration_text:
                pickVibration();
                break;
            case R.id.fab_alarm:
                done();
                break;
        }
    }

    private void pickAlarmFrequency(int setTo) {
        if (ALARM_MODE == setTo) return; // if nothing changed, do nothing

        LinearLayout chooseDateContainer = (LinearLayout) findViewById(R.id.choose_date_container);
        LinearLayout chooseDaysContainer = (LinearLayout) findViewById(R.id.choose_days_container);
        // LinearLayout chooseTimeContainer; --> always on screen!

        switch (setTo) {
            case C.ALARM_ONETIME:   // hide the days+time chooser and show the date chooser
                chooseDaysContainer.setVisibility(View.GONE);
                // -- //
                chooseDateContainer.setVisibility(View.VISIBLE);
                break;
            case C.ALARM_RECURRING: // hide the date chooser and show the days+time chooser
                chooseDateContainer.setVisibility(View.GONE);
                // -- //
                chooseDaysContainer.setVisibility(View.VISIBLE);
                break;
        }
        ALARM_MODE = setTo; // in the end, set the mode as current mode
    }

    private void pickDate() {
        datePicker = new Pickers.DatePickerFragment();
        datePicker.show(getFragmentManager(), "DatePickerDialog");
    }

    private void pickTime() {
        timePicker = new Pickers.TimePickerFragment();
        timePicker.show(getFragmentManager(), "TimePickerDialog");
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

        new AlertDialog.Builder(AlarmSetterActivity.this)
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
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        chosenDays = tempChoices.clone(); // assign our tempChoices to our "persistent" choices
                        ((TextView) findViewById(R.id.choose_days_text))
                                .setText(StringDisplay.getRecurringDays(AlarmSetterActivity.this, chosenDays));

                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
    }

    private void pickStation() {
        startActivityForResult(new Intent(AlarmSetterActivity.this, StationPickerActivity.class), C.REQUEST_STATION);
    }

    private void pickRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        this.startActivityForResult(intent, C.REQUEST_RINGTONE);
    }

    private void pickVibration() {
        final String[] vibrationModes = new String[]{
                getString(C.VIBRATION_STRINGS[C.VIBRATION_NONE]),
                getString(C.VIBRATION_STRINGS[C.VIBRATION_SHORT]),
                getString(C.VIBRATION_STRINGS[C.VIBRATION_MEDIUM]),
                getString(C.VIBRATION_STRINGS[C.VIBRATION_LONG])
        };

        new AlertDialog.Builder(AlarmSetterActivity.this)
                .setTitle(getString(R.string.alarm_choose_vibration_length))
                .setItems(vibrationModes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        chosenVibratorMode = which;
                        if (v.hasVibrator()) {
                            v.vibrate(C.VIBRATION_DURATION[which]);
                        }
                        ((TextView)findViewById(R.id.choose_vibration_text))
                                .setText(StringDisplay.getVibration(AlarmSetterActivity.this, which));
                    }
                })
                .show();
    }

    private void done() {
        // Errorcheck
        boolean isError = false;
        String errors = "";
        int[] date = datePicker.getChosenDate();
        int[] time = timePicker.getChosenTime();
        // Mode-specific checks
        if (ALARM_MODE == C.ALARM_ONETIME) {
            if (date == null) {
                isError = true;
                errors += getString(R.string.missing_info_date);
            }
            if (date != null && time != null) {
                Calendar now = Calendar.getInstance();
                Calendar calDate = Calendar.getInstance();
                calDate.set(date[0], date[1], date[2], time[0], time[1], 0); // 0 seconds
                if (calDate.compareTo(now) < 0) {
                    errors += getString(R.string.missing_info_past);
                }
            }
        } else if (ALARM_MODE == C.ALARM_RECURRING) {
            boolean noDays = true;
            for (boolean daySelected : chosenDays) {
                if (daySelected) { // if any day was set to true, no error
                    noDays = false;
                    break;
                }
            }
            if (noDays) {
                isError = true;
                errors += getString(R.string.missing_info_days);
            }
        }
        // General checks
        if (time == null) {
            isError = true;
            errors += getString(R.string.missing_info_time);
        }
        if (pickedStationData == null) {
            isError = true;
            errors += getString(R.string.missing_info_station);
        }
        // If error:
        if (isError) {
            AlertDialogs.missingInfo(AlarmSetterActivity.this, errors);
            return;
            // if error, we're done here
        }

        Alarm newAlarm = new Alarm();
        newAlarm.setAlarmMode(ALARM_MODE);
        switch (ALARM_MODE) {
            case C.ALARM_ONETIME:
                newAlarm.setOneTimeAlarmYear(date[0]);
                newAlarm.setOneTimeAlarmMonth(date[1]);
                newAlarm.setOneTimeAlarmDay(date[2]);
                break;
            case C.ALARM_RECURRING:
                newAlarm.setRecurringChosenDays(chosenDays);
                break;
        }
        newAlarm.setAlarmHour(time[0]);
        newAlarm.setAlarmMinute(time[1]);

        newAlarm.setChosenRingtone(chosenRingtone);
        newAlarm.setChosenVibrationMode(chosenVibratorMode);

        // {stationName, stationDir, stationId, h.getArrayIndex()}
        newAlarm.setStationName(pickedStationData[0]);
        newAlarm.setStationDirection(pickedStationData[1]);
        newAlarm.setStationId(pickedStationData[2]);
        newAlarm.setStationArrayIndex(Integer.parseInt(pickedStationData[3]));

        // TODO schedule alarm

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction(); // we're doing it synchronously because it does not take much time
        realm.copyToRealm(newAlarm);
        realm.commitTransaction();

        setResult(Activity.RESULT_OK, new Intent().putExtra("mode", ALARM_MODE));
        finish(); // we're done here.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case C.REQUEST_STATION:
                    pickedStationData = data.getStringArrayExtra("stationInfo");
                    ((TextView) findViewById(R.id.choose_station_text))
                            .setText(StringDisplay.getStation(pickedStationData[0], pickedStationData[1]));
                    break;
                case C.REQUEST_RINGTONE:
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    chosenRingtone = (uri != null) ? uri.toString() : null;

                    ((TextView)findViewById(R.id.choose_ringtone_text))
                            .setText(StringDisplay.getRingtone(AlarmSetterActivity.this, chosenRingtone));
                    break;
            }
        }
    }

    class GetApiFiles extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog warten;
        Context ctx;

        GetApiFiles(Context c) {
            ctx = c;
        }

        @Override
        protected void onPreExecute() {
            warten = new ProgressDialog(ctx);
            warten.setCancelable(false);
            warten.setIndeterminate(true);
            warten.setMessage(getString(R.string.updating_stations));
            warten.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Response<ResponseBody> versionResponse = RetrofitInfo.getCSVInfo().create(RetrofitInfo.CSVCalls.class).getVersionCSV().execute(); // check file version
                String versionResponseString = versionResponse.body().string();

                if (!versionResponse.isSuccessful()) return false;
                File csv = new File(ctx.getFilesDir(), C.CSV_FILENAME);
                String csvString = C.getCSVfromFile(ctx);
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
                    if (csv.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        csv.delete();
                    }
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
                AlertDialogs.serverNotAvailable(ctx);
                findViewById(R.id.choose_station_button).setEnabled(false); // disable station picker
            }
        }
    }

}
