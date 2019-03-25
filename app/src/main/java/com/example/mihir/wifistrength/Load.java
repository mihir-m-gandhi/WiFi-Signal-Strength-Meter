package com.example.mihir.wifistrength;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Load extends AppCompatActivity {

    private TextView loadedInfo;
    private TextView savedFiles;
    private static String FILE_NAME = "record001";
    private String filepath;
    public final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        loadedInfo = (TextView)findViewById(R.id.loadedInfo);
        savedFiles = (TextView)findViewById(R.id.savedFiles);
        filepath = getExternalFilesDir("Wifi Strength Results").toString();
        generateSavedFileList();
    }

    public void generateSavedFileList() {
        //String path = filepath;
        File directory = new File(filepath+"/");        //slash is very important, otherwise null pointer error
        File[] files = directory.listFiles();
        if(files.length!=0){
            savedFiles.setText("LIST OF SAVED FILES: \n\n");
            for (int i = 0; i < files.length; i++)
            {
                savedFiles.append(files[i].getName()+"\n");
            }
        }
        else{
            savedFiles.setText("Saved files will be shown here...");
        }
    }


    public void load(View view){

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
                                String absolutePath = filepath+"/"+userInput.getText().toString();
                                try {
                                    FileInputStream fis = new FileInputStream(absolutePath);
                                    DataInputStream in = new DataInputStream(fis);
                                    BufferedReader br =
                                            new BufferedReader(new InputStreamReader(in));
                                    String strLine;
                                    loadedInfo.setText(FILE_NAME+"\n\n");
                                    while ((strLine = br.readLine()) != null) {
                                        loadedInfo.append(strLine+"\n");
                                    }
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
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

    public void back(View view) {
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        startActivity(intent);
        super.onBackPressed();
    }
}
