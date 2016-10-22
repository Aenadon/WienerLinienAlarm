package aenadon.wienerlinienalarm;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import aenadon.wienerlinienalarm.models.Alarm;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

class AlarmListAdapter extends BaseAdapter {

    private String LOG_TAG = AlarmListAdapter.class.getSimpleName();

    private Context mContext;
    private int alarmModePage;

    private RealmResults<Alarm> alarms;

    private String[] stringSortOnetime = new String[]{
            "oneTimeAlarmYear",
            "oneTimeAlarmMonth",
            "oneTimeAlarmDay",
            "alarmHour",
            "alarmMinute"
    };
    private Sort[] orderSortOnetime = new Sort[]{
            Sort.ASCENDING,
            Sort.ASCENDING,
            Sort.ASCENDING,
            Sort.ASCENDING,
            Sort.ASCENDING
    };

    private String[] stringSortRecurring = new String[]{
            "alarmHour",
            "alarmMinute",
            "recurringChosenDays"
    };
    private Sort[] orderSortRecurring = new Sort[]{
            Sort.ASCENDING,
            Sort.ASCENDING,
            Sort.ASCENDING
    };


    AlarmListAdapter(Context c, int alarmModePage) {
        mContext = c;
        this.alarmModePage = alarmModePage;

        Realm.init(c);
        Realm realm = Realm.getDefaultInstance();

        String[] sortAfter;
        Sort[] sortOrder;

        if (alarmModePage == C.ALARM_ONETIME) {
            sortAfter = stringSortOnetime;
            sortOrder = orderSortOnetime;
        } else {
            sortAfter = stringSortRecurring;
            sortOrder = orderSortRecurring;
        }

        alarms = realm.where(Alarm.class).equalTo("alarmMode", alarmModePage).findAllSorted(sortAfter, sortOrder);
    }

    @Override public Object getItem(int position) {
        return null;
    }
    @Override public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return alarms.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater infl = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infl.inflate(R.layout.alarm_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.date = (TextView) convertView.findViewById(R.id.alarm_list_date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.alarm_list_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        String date, time;

        Alarm alarmItem = alarms.get(position);
        switch (alarmModePage) {
            case C.ALARM_ONETIME:
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(alarmItem.getOneTimeAlarmYear(),
                        alarmItem.getOneTimeAlarmMonth(),
                        alarmItem.getOneTimeAlarmDay(),
                        alarmItem.getAlarmHour(),
                        alarmItem.getAlarmMinute(),
                        0);

                date = DateFormat.getDateInstance().format(selectedDate.getTime());
                time = DateFormat.getTimeInstance(DateFormat.SHORT).format(selectedDate.getTime());
                break;
            case C.ALARM_RECURRING:
                boolean[] chosenDays = alarmItem.getRecurringChosenDays();

                String selection;
                if (!(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4] || chosenDays[5] || chosenDays[6])) {
                    selection = mContext.getString(R.string.alarm_no_days_set);  // then say "no days selected"
                } else if (!(chosenDays[0] || chosenDays[1] || chosenDays[2] || chosenDays[3] || chosenDays[4]) && (chosenDays[5] && chosenDays[6])) {
                    selection = mContext.getString(R.string.weekends);
                } else if ((chosenDays[0] && chosenDays[1] && chosenDays[2] && chosenDays[3] && chosenDays[4]) && !(chosenDays[5] || chosenDays[6])) {
                    selection = mContext.getString(R.string.weekdays);
                } else if (chosenDays[0] && chosenDays[1] && chosenDays[2] && chosenDays[3] && chosenDays[4] && chosenDays[5] && chosenDays[6]) {
                    selection = mContext.getString(R.string.everyday);
                } else {
                    int selectedDays = 0;
                    for (int i = 0; i < 7; i++) {
                        if (chosenDays[i]) selectedDays++;
                    }
                    selection = mContext.getResources().getQuantityString(R.plurals.days_chosen, selectedDays, selectedDays); // else show the count of days chosen
                }
                date = selection;
                time = String.format(Locale.ENGLISH, "%02d:%02d", alarmItem.getAlarmHour(), alarmItem.getAlarmMinute());
                break;
            default:
                Log.e(LOG_TAG, "Page index out of range");
                return null;
        }

        viewHolder.date.setText(date);
        viewHolder.time.setText(time);

        return convertView;
    }

    private class ViewHolder {
        TextView date;      // @+id/alarm_list_date
        TextView time;      // @+id/alarm_list_time
    }
}
