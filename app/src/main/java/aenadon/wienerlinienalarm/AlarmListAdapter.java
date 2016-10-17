package aenadon.wienerlinienalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AlarmListAdapter extends BaseAdapter {

    private Context mContext;
    private int pageNumber;

    AlarmListAdapter(Context c, int pageNumber) {
        mContext = c;
        this.pageNumber = pageNumber;
    }

    @Override public Object getItem(int position) {
        return null;
    }
    @Override public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return testValues1.length;
    }

    private String[] testValues1 = {
            "17.10.2016",
            "27.10.2016",
            "17.10.2016",
            "27.10.2016",
            "17.10.2016",
            "27.10.2016",
            "17.10.2016",
            "27.10.2016",
            "17.10.2016",
            "27.10.2016",

    };
    private String[] testValues2 = {
            "19:30",
            "09:30",
            "22:40",
            "07:37",
            "23:59",
            "11:11",
            "11:11",
            "26:62",
            "26:62",
            "26:62",
    };
    private String[] testValues3 = new String[]{
            "Mon-Tue, Thu-Fri, Sat-Sun",
            "Mon,Tue,Wed,Thu,Fri,Sat",
            "LLL,LLL,LLL,LLL,LLL,LLL",// test max width 6days
            "Mon,Tue,Wed,Thu,Fri,Sat,Sun", // max max width 7days - not recommended
            "Every day",
            "Weekdays",
            "Weekends",
            "Weekends",
            "Weekends",
            "Weekends",
    };
    private String[] testValues4 = {
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
            "10:00",
    };

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater infl = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = infl.inflate(R.layout.alarm_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.station = (TextView) convertView.findViewById(R.id.alarm_list_station);
            viewHolder.direction = (TextView) convertView.findViewById(R.id.alarm_list_direction);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        String[] testValuesWeAreGoingToUse1 = (pageNumber == 0) ? testValues1 : testValues3;
        String[] testValuesWeAreGoingToUse2 = (pageNumber == 0) ? testValues2 : testValues4;

        viewHolder.station.setText(testValuesWeAreGoingToUse1[position]);
        viewHolder.direction.setText(testValuesWeAreGoingToUse2[position]);

        return convertView;
    }

    private class ViewHolder {
        TextView station;       // @+id/alarm_list_station
        TextView direction;     // @+id/alarm_list_direction
    }
}
