package com.red.team.app;

import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Favela on 11/14/2017
 */

public class SettingsActivity extends Activity {

    private String[] defaultMenu;
    private ArrayList<String> list;
    private Set<String> mset;

    Intent intent = new Intent(this,SettingsActivity.class);

    // OnCreate, called once to initialize the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        SharedPreferences mPrefs = getSharedPreferences("list",MODE_PRIVATE);
        final SharedPreferences.Editor mEditor = mPrefs.edit();

        // Grab references to UI elements.
        final ListView listView = (ListView) findViewById(R.id.listView);
        defaultMenu = new String[]{"Lights 1", "Lights 2", "TV","Call For Nurse", "End"};


        if (savedInstanceState != null) {
            list = savedInstanceState.getStringArrayList("savedList");
            mEditor.putStringSet("listSet", new HashSet<>(list));
            mEditor.commit();
        }

        mset = mPrefs.getStringSet("listSet",new HashSet<>(Arrays.asList(defaultMenu)));
        mEditor.commit();

        list = new ArrayList<>(mset);

        final ButtonAdapter buttonAdapter = new ButtonAdapter(list, this);
        listView.setAdapter(buttonAdapter);

        ImageButton addDevice = (ImageButton) findViewById(R.id.Add);
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

                    mEditor.putStringSet("listSet", new HashSet<>(list));
                    mEditor.commit();

                    textView.setText("");

                    handled = true;
                }
                return handled;
            }
        });

    }

    //Saves inputted list when changing orientation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("savedList",list);
    }


    //Returns inputted list when changing orientation
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        SharedPreferences mPrefs = getSharedPreferences("list",MODE_PRIVATE);
        final SharedPreferences.Editor mEditor = mPrefs.edit();

        list = savedInstanceState.getStringArrayList("savedList");

        mEditor.putStringSet("listSet", new HashSet<>(list));
        mEditor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences mPrefs = getSharedPreferences("list",MODE_PRIVATE);
        final SharedPreferences.Editor mEditor = mPrefs.edit();

        mEditor.putStringSet("listSet", new HashSet<>(list));
        mEditor.commit();

        intent.putExtra("list",list);
    }

}
