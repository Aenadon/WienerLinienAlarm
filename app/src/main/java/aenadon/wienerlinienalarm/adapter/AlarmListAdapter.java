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

public class AlarmListAdapter extends BaseAdapter {

    private final Context ctx;

    private final List<Alarm> alarms;

    public AlarmListAdapter(Context ctx, AlarmType alarmType) {
        this.ctx = ctx;

        Realm realm = Realm.getDefaultInstance();
        // TODO sort elements
        RealmResults<Alarm> realmAlarms = realm.where(Alarm.class)
                .equalTo("alarmType", alarmType.toString())
                .findAll();
        alarms = realm.copyFromRealm(realmAlarms);
        realm.close();
    }

    @Override
    public Object getItem(int position) {
        return alarms.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
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
            viewHolder.date = (TextView) convertView.findViewById(R.id.alarm_list_date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.alarm_list_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        Alarm alarm = alarms.get(position);

        String date = StringDisplay.getDate(ctx, alarm);
        String time = StringDisplay.getTime(alarm.getAlarmTime());

        viewHolder.date.setText(date);
        viewHolder.time.setText(time);

        return convertView;
    }

    private class ViewHolder {
        TextView date;      // @+id/alarm_list_date
        TextView time;      // @+id/alarm_list_time
    }
}
