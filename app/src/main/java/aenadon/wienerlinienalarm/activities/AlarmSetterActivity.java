package aenadon.wienerlinienalarm.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import aenadon.wienerlinienalarm.utils.AlertDialogs;
import aenadon.wienerlinienalarm.utils.Const;
import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.CSVWorkUtils;
import aenadon.wienerlinienalarm.utils.RetrofitInfo;
import aenadon.wienerlinienalarm.models.Alarm;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class AlarmSetterActivity extends AppCompatActivity {

    private int ALARM_MODE = Const.ALARM_ONETIME;

    private Pickers.DatePickerFragment datePicker = new Pickers.DatePickerFragment();
    private Pickers.TimePickerFragment timePicker = new Pickers.TimePickerFragment();
    private Pickers.DaysPicker daysPicker = new Pickers.DaysPicker();
    private Pickers.RingtonePicker ringtonePicker = new Pickers.RingtonePicker();
    private Pickers.VibrationPicker vibrationPicker = new Pickers.VibrationPicker();
    private Pickers.StationPicker stationPicker = new Pickers.StationPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        // if device has no vibrator, hide the vibration choice
        Vibrator v = (Vibrator)getSystemService(VIBRATOR_SERVICE);
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
                pickAlarmFrequency(Const.ALARM_ONETIME);
                break;
            case R.id.radio_frequency_recurring:
                pickAlarmFrequency(Const.ALARM_RECURRING);
                break;
            case R.id.choose_date_button:
            case R.id.choose_date_text:
                Bundle a = new Bundle();
                a.putInt("viewToUse", R.id.choose_date_text);
                datePicker.setArguments(a);
                datePicker.show(getFragmentManager(), "DatePickerDialog");
                break;
            case R.id.choose_time_button:
            case R.id.choose_time_text:
                Bundle b = new Bundle();
                b.putInt("viewToUse", R.id.choose_time_text);
                timePicker.setArguments(b);
                timePicker.show(getFragmentManager(), "TimePickerDialog");
                break;
            case R.id.choose_days_button:
            case R.id.choose_days_text:
                daysPicker.show(AlarmSetterActivity.this, null, R.id.choose_days_text);
                break;
            case R.id.choose_station_button:
            case R.id.choose_station_text:
                stationPicker.show(AlarmSetterActivity.this, R.id.choose_station_text);
                break;
            case R.id.choose_ringtone_button:
            case R.id.choose_ringtone_text:
                ringtonePicker.show(AlarmSetterActivity.this, R.id.choose_ringtone_text);
                break;
            case R.id.choose_vibration_button:
            case R.id.choose_vibration_text:
                vibrationPicker.show(AlarmSetterActivity.this, R.id.choose_vibration_text);
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
            case Const.ALARM_ONETIME:   // hide the days+time chooser and show the date chooser
                chooseDaysContainer.setVisibility(View.GONE);
                // -- //
                chooseDateContainer.setVisibility(View.VISIBLE);
                break;
            case Const.ALARM_RECURRING: // hide the date chooser and show the days+time chooser
                chooseDateContainer.setVisibility(View.GONE);
                // -- //
                chooseDaysContainer.setVisibility(View.VISIBLE);
                break;
        }
        ALARM_MODE = setTo; // in the end, set the mode as current mode
    }

    private void done() {
        // Errorcheck
        boolean isError = false;
        String errors = "";

        int[] chosenDate = datePicker.getPickedDate();
        int[] chosenTime = timePicker.getPickedTime();
        boolean[] chosenDays = daysPicker.getPickedDays();
        String chosenRingtone = ringtonePicker.getPickedRingtone();
        int chosenVibratorMode = vibrationPicker.getPickedVibrationMode();

        // Mode-specific checks
        if (ALARM_MODE == Const.ALARM_ONETIME) {
            if (chosenDate == null) {
                isError = true;
                errors += getString(R.string.missing_info_date);
            }
            if (chosenDate != null && chosenTime != null) {
                Calendar now = Calendar.getInstance();
                Calendar calDate = Calendar.getInstance();
                calDate.set(chosenDate[0], chosenDate[1], chosenDate[2], chosenTime[0], chosenTime[1], 0); // 0 seconds
                if (calDate.compareTo(now) < 0) {
                    errors += getString(R.string.missing_info_past);
                }
            }
        } else if (ALARM_MODE == Const.ALARM_RECURRING) {
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
        if (chosenTime == null) {
            isError = true;
            errors += getString(R.string.missing_info_time);
        }
        if (!stationPicker.stationWasSet()) {
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
            case Const.ALARM_ONETIME:
                newAlarm.setOneTimeAlarmYear(chosenDate[0]);
                newAlarm.setOneTimeAlarmMonth(chosenDate[1]);
                newAlarm.setOneTimeAlarmDay(chosenDate[2]);
                break;
            case Const.ALARM_RECURRING:
                newAlarm.setRecurringChosenDays(chosenDays);
                break;
        }
        newAlarm.setAlarmHour(chosenTime[0]);
        newAlarm.setAlarmMinute(chosenTime[1]);

        newAlarm.setChosenRingtone(chosenRingtone);
        newAlarm.setChosenVibrationMode(chosenVibratorMode);

        // {stationName, stationDir, stationId, h.getArrayIndex()}
        newAlarm.setStationName(stationPicker.getPickedStationName());
        newAlarm.setStationDirection(stationPicker.getPickedStationDir());
        newAlarm.setStationId(stationPicker.getPickedStationId());
        newAlarm.setStationArrayIndex(stationPicker.getPickedStationArrayIndex());

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
                case Const.REQUEST_STATION:
                    stationPicker.setPickedStation(data);
                    break;
                case Const.REQUEST_RINGTONE:
                    ringtonePicker.setPickedRingtone(AlarmSetterActivity.this, data);
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
                File csv = new File(ctx.getFilesDir(), Const.CSV_FILENAME);
                String csvString = CSVWorkUtils.getCSVfromFile(ctx);
                if (csv.exists() && csvString != null) {
                    String x = csvString.split(Const.CSV_FILE_SEPARATOR)[Const.CSV_PART_VERSION];
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
                                    + Const.CSV_FILE_SEPARATOR +             // separator
                                    haltestellenResponse.body().string() // haltestellen CSV
                                    + Const.CSV_FILE_SEPARATOR +             // separator
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
