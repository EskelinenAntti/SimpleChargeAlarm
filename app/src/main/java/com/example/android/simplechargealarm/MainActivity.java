package com.example.android.simplechargealarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set toggle-button to control foreground service.
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle_button);

        // Check if service is already running and set the state of the toggle-button according to
        // it.
        toggle.setChecked(ForegroundService.serviceIsRunning);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Start the foreground service when the toggle-button is enabled.
                    Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                    startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                    startService(startIntent);
                    Log.v("sdgöasdlö",isChecked+"");
                } else {
                    // Stop the foreground service when the toggle-button is disabled.
                    Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
                    stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                    startService(stopIntent);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
