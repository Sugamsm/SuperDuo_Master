package it.jaschke.alexandria;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by HP on 04-02-2016.
 */
public class LVAdapter extends BaseAdapter {

    private List<UIData> data = Collections.emptyList();
    Context context;

    public LVAdapter(Context context, List<UIData> data) {
        this.context = context;
        this.data = data;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.single_nav_item, parent, false);

        TextView tv = (TextView) v.findViewById(R.id.nav_head);
        ImageView img = (ImageView) v.findViewById(R.id.nav_icon);

        UIData current = data.get(position);
        tv.setText(current.head);
        img.setImageResource(current.icon);

        return v;
    }
}
