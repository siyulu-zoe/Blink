package com.red.team.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * Created by David Favela on 11/15/2017.
 **/

public class ButtonAdapter extends BaseAdapter implements ListAdapter{
    private ArrayList<String> list = new ArrayList<String>();
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

        TextView listItemText = (TextView)view.findViewById(R.id.textView);
        listItemText.setText(list.get(position));


//        ImageButton deletebtn = (ImageButton) view.findViewById(R.id.Delete);
//
//        deletebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                list.remove(position);
//                notifyDataSetChanged();
//            }
//        });

        ToggleButton onoff_toggle = (ToggleButton) view.findViewById(R.id.Delete);
        onoff_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                } else {
                    // The toggle is disabled
                }
            }
        });

        return view;
    }
}
