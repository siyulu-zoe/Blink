package com.red.team.app;

import android.app.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by David Favela on 11/14/2017
 */


public class SettingsActivity extends Activity {

    private String[] values;

    // OnCreate, called once to initialize the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Grab references to UI elements.
        final ListView listView = (ListView) findViewById(R.id.listView);
        final String[] menu = new String[]{"Lamp", "TV", "Call for Nurse"};
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < menu.length; ++i) {
            list.add(menu[i]);
        }

        final ButtonAdapter buttonAdapter = new ButtonAdapter(list, this);
        listView.setAdapter(buttonAdapter);

        TextView addDevice = (TextView) findViewById(R.id.Add);
        final EditText addText = (EditText) findViewById(R.id.AddText);

        //toggles input text box visibility
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addText.getVisibility()==View.GONE) {
                    addText.setVisibility(View.VISIBLE);
                }

                else {
                    addText.setVisibility(View.GONE);
                }
            }
        });

        //adds devices to menu
        addText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_DONE) {
                    list.add(addText.getText().toString());
                    ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
                    System.out.println(list);
                    handled = true;
                }
                return handled;
            }
        });

    }
}