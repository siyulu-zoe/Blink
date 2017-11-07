package com.red.team.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_menu);

            final ListView listview = (ListView) findViewById(R.id.main_list);
            String[] values = new String[] { "Get Blinking!", "Calibration", "Settings"};
            final ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < values.length; ++i) {
                list.add(values[i]);
            }
            final StableArrayAdapter adapter = new StableArrayAdapter(this,
                    android.R.layout.simple_list_item_1, list);
            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    switch(item) {
                        case "Get Blinking!":
                            Intent intent = new Intent(MainActivity.this , BlinkActivity.class);
                            startActivity(intent);
                            break;
                        case "Calibration":
                            break;
                        case "Settings":
                            break;
                    }
                }

            });
        }


}



