package com.red.team.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends Activity {

        private TextToSpeech t1;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main_menu);
            final ListView listview = (ListView) findViewById(R.id.main_list);
            String[] values = new String[] { "Start Blinking!", "Calibration", "Settings"};
            final ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < values.length; ++i) {
                list.add(values[i]);
            }
            final StableArrayAdapter adapter = new StableArrayAdapter(this,
                    android.R.layout.simple_list_item_1, list);
            listview.setAdapter(adapter);

            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR){
                        t1.setLanguage(Locale.UK);
                    }
                    t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }
                        @Override
                        public void onDone(String utteranceId) {
                        }
                        @Override
                        public void onError(String utteranceId) {
                        }
                    });
                }
            });

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    switch(item) {
                        case "Start Blinking!":
                            Intent intent = new Intent(MainActivity.this , BlinkActivity.class);
                            startActivity(intent);
                            break;
                        case "Calibration":
                            calibration();
                            break;
                        case "Settings":
                            break;
                    }
                }

            });
        }

    // Voice guide for the calibration process in setting up Blink.
    private void calibration() {
        String Calibration = "Hello, welcome to Blink, your personal eye remote. Before you start using Blink, we need to calibrate the sensors. Close your eyes now for half a second. Close your eyes now for three seconds. Three, Two, One. Thank you. You may now start using Blink. ";
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "calibration");
        t1.speak(Calibration,TextToSpeech.QUEUE_FLUSH, params,"calibration");
    }
}
