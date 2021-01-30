package com.example.mihir.wifistrength;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    private TextView WifiInfo;
    //private TextView infoFromFile;
    private ListView listView;
    private Button saveButton;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> wifiList = new ArrayList<>();
    private ArrayAdapter adapter;
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    public final Context context = this;
    private static String FILE_NAME = "record001";
    private String filepath;
    File filePath;
    int count=0;
    int stop=0;
    int sum=0;
    double average=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiInfo = (TextView)findViewById(R.id.info);
        //infoFromFile = (TextView)findViewById(R.id.infoFromFile);
        saveButton = (Button)findViewById(R.id.saveButton);
        listView = (ListView)findViewById(R.id.wifiList);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wifi is disbaled, enabling Wifi", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wifiList);
        listView.setAdapter(adapter);
        filepath = getExternalFilesDir("Wifi Strength Results").toString();
        //generateSavedFileList();

    }


    public void getWifiInfo(View view) {
            WifiInfo.setText("");
            stop = 0;
            count=0;
            if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                saveButton.setEnabled(false);
            }

            wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();

            if(wifiInfo.getNetworkId()!=-1){
                int ip = wifiInfo.getIpAddress();
                @SuppressWarnings("deprecation") String ipAddress = Formatter.formatIpAddress(ip);
                String macAddress = wifiInfo.getMacAddress();
                int linkSpeed = wifiInfo.getLinkSpeed();
                int networkId = wifiInfo.getNetworkId();
                int frequency = wifiInfo.getFrequency();
                String ssid = wifiInfo.getSSID();
                String bssid = wifiInfo.getBSSID();

                int numberOfLevels = 5;
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
                String info = ("IP Address: "+ipAddress+"\nMAC Address: "+macAddress+"\nNetwork ID: "+networkId+"\nSSID: "+ssid+"\nBSSID: "+bssid+"\nLink Speed: "+linkSpeed+" Mbps"+"\nFrequency: "+frequency+" MHz"+"\nLevel: "+level+"/5\n\n");
                WifiInfo.append(info);


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(count<60 && stop==0){
                            getRssiLevel(count);
                            handler.postDelayed(this, 1000);
                        }

                    }
                }, 1000);  //the time is in miliseconds
            }
            else{
                Toast.makeText(this, "You are not connected to a Wifi Network", Toast.LENGTH_SHORT).show();
            }
    }

    public void getRssiLevel(int t) {
        wifiInfo = wifiManager.getConnectionInfo();
        int rssiLevel = wifiInfo.getRssi();
        sum = sum + rssiLevel;
        String tempInfo = ((t+1)+"s --> RSSI Level: "+rssiLevel+"\n");
        WifiInfo.append(tempInfo);
        count++;
        if(count==60){
            average = sum/60;
            WifiInfo.append("Average RSSI Level: "+average + "dbm");
        }
    }

    public void getWifiList(View view) {
        wifiList.clear();
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Log.d("mihir", "reached");
        Toast.makeText(this, "Scanning WiFi", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                wifiList.add(scanResult.SSID);
                adapter.notifyDataSetChanged();
            }
            Log.d("mihir", String.valueOf(results.size()));
        }
    };

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public void save(View v) {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                FILE_NAME = userInput.getText().toString();
                                //filepath = getExternalFilesDir("Wifi Strength Results").toString();
                                File file = new File(filepath, FILE_NAME);
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(WifiInfo.getText().toString().getBytes());
                                    fos.close();
                                     Log.d("mihir: ", filepath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(context, "File saved at "+filepath, Toast.LENGTH_SHORT).show();
                                WifiInfo.setText("Information will appear here...");
                                //generateSavedFileList();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private boolean isExternalStorageWritable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.v("State","Yes");
            return true;
        }
        else { return false;}
    }

    public boolean checkPermission(String permission) {
        int check  = ContextCompat.checkSelfPermission(this, permission);
        return (check== PackageManager.PERMISSION_GRANTED);
    }



    public void stopInfo(View view) {
        stop=1;
    }

    public void viewSaved(View view) {
        Intent intent = new Intent(getApplicationContext(), Load.class);
        startActivity(intent);

    }
}