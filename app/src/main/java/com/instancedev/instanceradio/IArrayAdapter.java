package com.instancedev.instanceradio;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class IArrayAdapter extends ArrayAdapter {
    public IArrayAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (MainActivity.activity.serv.currentid != position){
            v.setBackgroundColor(Color.TRANSPARENT);
        } else {
            v.setBackgroundColor(Color.parseColor("#4587ceeb"));
        }

        return v;
    }
}
