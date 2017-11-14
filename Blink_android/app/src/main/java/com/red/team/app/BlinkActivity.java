package com.red.team.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.os.Handler;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_FLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;

public class BlinkActivity extends Activity {

    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UI elements
    private TextView messages;
    private EditText input;

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    private String[] values;
    private ArrayList<String> list;
    private ListView listview;
    private int current_list_value;
    private int current_list_size;
    private String current_list_item;
    private Boolean click = false;
    private Boolean reading = false;
    private Boolean action = false;
    private Boolean ready = false;
    private Boolean leaf = false;
    private StableArrayAdapter list_adapter;
    private TextToSpeech t1;

    // OnCreate, called once to initialize the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blink_activity);

        // Grab references to UI elements.
        messages = (TextView) findViewById(R.id.messages);
        listview = (ListView) findViewById(R.id.actions);
        values = new String[] { "Light 1", "Light 2", "TV", "End"};
        list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        list_adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        list_adapter.notifyDataSetChanged();
        listview.setAdapter(list_adapter);
        current_list_size = values.length;
        current_list_value = 0;
        current_list_item = values[current_list_value];
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
                        ready = true;
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
            }
        });

        //switch code
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
                action(item);
            }
        });
        adapter = BluetoothAdapter.getDefaultAdapter();
    }


    // Main BTLE device callback where much of the logic occurs.
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("Failed to start discovering services!");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("Disconnected!");
            }
            else {
                writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Service discovery completed!");
            }
            else {
                writeLine("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String va = characteristic.getStringValue(0);
            writeLine("Received: " + va);
            if (va.length() < 4) {
                if (reading == false) {
                    reading = true;
                    ready = true;
                    new main_thread().execute();
                }
                else {
                    reading = false;
                    action("End");
                }
            }
            else {
                if (reading == true) {
                    action = true;
                }
                else {
                }
            }
        }
    };

    // OnResume, called right before UI is displayed.  Start the BTLE connection.
    @Override
    protected void onResume() {
        super.onResume();
        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.
        messages.setText("Scanning for devices...");
        adapter.startLeScan(scanCallback);
    }

    // BTLE device scanning callback.
    private LeScanCallback scanCallback = new LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            writeLine("Found device: " + bluetoothDevice.getAddress());
            // Check if the device has the UART service.
            if (parseUUIDs(bytes).contains(UART_UUID)) {
                // Found a device, stop the scan.
                adapter.stopLeScan(scanCallback);
                writeLine("Found UART service!");
                // Connect to the device.
                // Control flow will now go to the callback functions when BTLE events occur.
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
            }
        }
    };


    // OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
    @Override
    protected void onStop() {
        super.onStop();
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }
    }

    // Handler for mouse click on the send button.
    public void sendClick(View view) {
        String message = input.getText().toString();
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        if (gatt.writeCharacteristic(tx)) {
            writeLine("Sent: " + message);
        }
        else {
            writeLine("Couldn't write TX characteristic!");
        }
    }

    // Write some text to the messages text view.
    // Care is taken to do this on the main UI thread so writeLine can be called
    // from any thread (like the BTLE callback).
    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");
            }
        });
    }

    // Filtering by custom UUID is broken in Android 4.3 and 4.4, see:
    //   http://stackoverflow.com/questions/18019161/startlescan-with-128-bit-uuids-doesnt-work-on-native-android-ble-implementation?noredirect=1#comment27879874_18019161
    // This is a workaround function from the SO thread to manually parse advertisement data.
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }
    
    // Loop through cascading menu options. Stop reading once a blink is registered.
    private void read_list() {
        String Test1 = current_list_item;
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "test");
        t1.speak(Test1,TextToSpeech.QUEUE_FLUSH, params,"test");
        ready = false;
    }
    
    // Voice commands to smart home device.
    private void read_command(String input, String smart_home_device) {
        String Command = "Hey " + smart_home_device + ", ";
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "command");
        switch (input) {
            case "Light 1 On":
                Command += "turn on light 1.";
                break;
            case "Light 1 Off":
                Command += "turn off light 1.";
                break;
            case "Brighten Light 1":
                Command += "brighten light 1.";
                break;
            case "Dim Light 1":
                Command += "dim light 1.";
                break;
            case "Light 2 On":
                Command += "turn on light 2.";
                break;
            case "Light 2 Off":
                Command += "turn off light 2.";
                break;
            case "Brighten Light 2":
                Command += "brighten light 2.";
                break;
            case "Dim Light 2":
                Command += "dim light 2.";
                break;
            case "TV On":
                Command += "turn on the TV.";
                break;
            case "TV Off":
                Command += "turn off the TV.";
                break;
            case "Channel Up":
                Command += "go to the next channel on the TV.";
                break;
            case "Channel Down":
                Command += "go to the previous channel on the TV.";
                break;
        }
        t1.speak(Command,TextToSpeech.QUEUE_FLUSH, params,"command");
        ready = false;
    }
    
    // Cascading menu options
    private void action(String selected_value) {
        switch (selected_value) {
            case "Light 1":
                values = new String[] { "Light 1 On","Light 1 Off", "Dim Light 1","Brighten Light 1","Back"};
                break;
            case "Light 2":
                values = new String[] { "Light 2 On","Light 2 Off", "Dim Light 2","Brighten Light 2","Back"};
                break;
            case "TV":
                values = new String[] { "TV On","TV Off", "Channel Up","Channel Down","Back"};
                break;
            case "Back":
                values = new String[] { "Light 1", "Light 2", "TV", "End"};
                break;
            case "End":
                values = new String[] { "Light 1", "Light 2", "TV", "End"};
                reading = false;
                break;
            default:
                values = new String[] { "Light 1", "Light 2", "TV", "End"};
                read_command(selected_value,"Google"); //"Google" can be switched to "Alexa"
                reading = false;
                break;
        }

        list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }
        list_adapter = new StableArrayAdapter(getBaseContext(),
                android.R.layout.simple_list_item_1, list);
        current_list_value = 0;
        current_list_size = values.length;
        current_list_item = values[current_list_value];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listview.setAdapter(list_adapter);
            }
        });
    }

    private class main_thread extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (reading==true){
                while (ready == false) {
                }
                    read_list();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (action == false) {
                    if (current_list_value == current_list_size - 1) {
                        current_list_value = 0;
                    } else {
                        current_list_value = current_list_value + 1;

                    }
                    current_list_item = values[current_list_value];

                } else {
                    action(current_list_item);
                    action = false;
                }
            }
            return null;
        }
    }
}
