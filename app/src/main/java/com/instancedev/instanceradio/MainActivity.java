package com.instancedev.instanceradio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {
    public iService serv;
    boolean bound;
    boolean playing = false;
    boolean init = false;

    public static MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;

        final Intent serviceIntent = new Intent(this, iService.class);

        System.out.println(serviceIntent.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageButton button1 = (ImageButton) findViewById(R.id.button);
        button1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (!playing) {
                    if (!init) {
                        init = true;
                        bindService(serviceIntent, servConnection, Context.BIND_AUTO_CREATE);
                        startService(serviceIntent);
                        v.setEnabled(false);
                        findViewById(R.id.layout1).setVisibility(View.VISIBLE);
                    } else {
                        serv.mp.start();
                    }
                    button1.setImageResource(R.drawable.pause);
                } else {
                    serv.mp.pause();
                    button1.setImageResource(R.drawable.arrow_right);
                }
                playing = !playing;
            }
        });

        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (bound) {
                    button1.setEnabled(false);
                    button2.setEnabled(false);
                    if (!playing) {
                        playing = !playing;
                        button1.setImageResource(R.drawable.pause);
                    }
                    serv.playNext();
                }
            }
        });

        final ListView lv = (ListView) findViewById(R.id.listView);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Object o = lv.getItemAtPosition(position);
                int clickedIndex = serv.titles.indexOf(o);
                serv.currentid = clickedIndex - 1;
                serv.playNext();
            }
        });

    }

    private ServiceConnection servConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            iService.LocalBinder binder = (iService.LocalBinder) service;
            serv = binder.getService();
            bound = true;
            serv.cancelNotification(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(servConnection);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bound) {
            serv.cancelNotification(true);
            unbindService(servConnection);
            bound = false;
            stopService(new Intent(this, iService.class));
        }
    }

}