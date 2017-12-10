package com.red.team.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import static android.graphics.Color.argb;


/**
 * Created by David Favela on 11/15/2017.
 **/

public class ButtonAdapter extends BaseAdapter implements ListAdapter{
    private ArrayList<String> list = new ArrayList<>();
    private Context context;


    public ButtonAdapter(ArrayList<String> list, Context context){
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.button, null);
        }

        final TextView listItemText = (TextView)view.findViewById(R.id.textView);
        listItemText.setText(list.get(position));

        final Switch settingsSwitch = (Switch) view.findViewById(R.id.settingsSwitch);

        settingsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean switchState = settingsSwitch.isChecked();
                if (switchState) {
                    settingsSwitch.setChecked(false);
                    listItemText.setTextColor(argb(100,0,0,0));
                    notifyDataSetChanged();
                } else {
                    settingsSwitch.setChecked(true);
                    listItemText.setTextColor(argb(255,0,0,0));
                    notifyDataSetChanged();
                }
            }
        });

        return view;
    }
}
