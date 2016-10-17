package aenadon.wienerlinienalarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ResultAdapter extends ArrayAdapter<Halteobjekt> {

    private final Context context;
    private final ArrayList<Halteobjekt> values;

    ResultAdapter(Context context, ArrayList<Halteobjekt> list) {
        super(context, -1, list);
        this.context = context;
        this.values = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ResultViewHolder resultView;
        LayoutInflater infl = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) { // view is uninitialized yet
            //convertView = infl.inflate(R.layout.list_item_single, null);
            convertView = infl.inflate(android.R.layout.simple_list_item_1, null);
            resultView = new ResultViewHolder();
            resultView.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(resultView);
        } else {
            resultView = (ResultViewHolder) convertView.getTag();
        }
        resultView.textView.setText(values.get(position).getName());

        return convertView;
    }

    private static class ResultViewHolder {
        TextView textView;
    }
}
