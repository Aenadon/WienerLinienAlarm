package aenadon.wienerlinienalarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.AlarmType;
import aenadon.wienerlinienalarm.models.alarm.Alarm;
import aenadon.wienerlinienalarm.utils.StringDisplay;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class AlarmListAdapter extends BaseAdapter {

    private final Context ctx;
    private AlarmType alarmType;
    private List<Alarm> alarms;

    public AlarmListAdapter(Context ctx, AlarmType alarmType) {
        this.ctx = ctx;
        this.alarmType = alarmType;
        getAlarmsFromDatabase(alarmType);
    }

    @Override
    public void notifyDataSetChanged() {
        getAlarmsFromDatabase(alarmType);
        super.notifyDataSetChanged();
    }

    private void getAlarmsFromDatabase(AlarmType alarmType) {
        // It's probably cheaper memory-wise to open and close a connection when needed
        // than having a Realm DB connection open for the lifetime of this adapter
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Alarm> realmAlarms = realm.where(Alarm.class)
                .equalTo("alarmType", alarmType.toString())
                .findAllSorted(sortingCriteria(alarmType), sortingOrder(alarmType));
        alarms = realm.copyFromRealm(realmAlarms);
        realm.close();
    }

    private String[] sortingCriteria(AlarmType alarmType) {
        if (alarmType == AlarmType.ONETIME) {
            return new String[]{
                    "onetimeAlarmYear",
                    "onetimeAlarmMonth",
                    "onetimeAlarmDay",
                    "alarmHour",
                    "alarmMinute"
            };
        } else {
            return new String[]{
                    "alarmHour",
                    "alarmMinute",
                    "recurringChosenDays"
            };
        }
    }

    private Sort[] sortingOrder(AlarmType alarmType) {
        if (alarmType == AlarmType.ONETIME) {
            return new Sort[]{
                    Sort.ASCENDING,
                    Sort.ASCENDING,
                    Sort.ASCENDING,
                    Sort.ASCENDING,
                    Sort.ASCENDING
            };
        } else {
            return new Sort[]{
                    Sort.ASCENDING,
                    Sort.ASCENDING,
                    Sort.ASCENDING
            };
        }
    }

    @Override
    public Object getItem(int position) {
        return alarms.get(position);
    }

    @Override
    public long getItemId(int position) {
        // the alarms don't have long IDs
        return position;
    }

    public String getAlarmId(int position) {
        return alarms.get(position).getId();
    }

    @Override
    public int getCount() {
        return alarms.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater infl = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infl.inflate(R.layout.list_item_alarm, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.date = convertView.findViewById(R.id.alarm_list_date);
            viewHolder.time = convertView.findViewById(R.id.alarm_list_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Alarm alarm = alarms.get(position);

        String date = StringDisplay.getDate(ctx, alarm);
        String time = StringDisplay.formatLocalTime(alarm.getAlarmTime());

        viewHolder.date.setText(date);
        viewHolder.time.setText(time);

        return convertView;
    }

    private class ViewHolder {
        TextView date;
        TextView time;
    }
}
