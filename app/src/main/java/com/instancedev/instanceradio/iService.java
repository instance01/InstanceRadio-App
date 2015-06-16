package com.instancedev.instanceradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class iService extends Service implements OnCompletionListener, OnPreparedListener, OnErrorListener {

    private final IBinder binder = new LocalBinder();
    public MediaPlayer mp = new MediaPlayer();
    private String url;

    private static final HashMap<String, String> channels;

    static {
        channels = new HashMap<>();
        channels.put("LDM", "UUDl6xIISC4tm38lzmcHvDiQ");
        channels.put("PerfectElectroMusic", "UUtCcPJl-cIG-mRiIyg-PKsQ");
        channels.put("xKito", "UUMOgdURr7d8pOVlc-alkfRg");
        channels.put("xKito2", "UUE_4AzEG60_GRBg4im9tKHg");
        channels.put("MrSuicideSheep", "UU5nc_ZtjKW1htCVZVRxlQAQ");
        channels.put("NoCopyrightSounds", "UU_aEa8K-EOJ3D6gOs7HcyNg");
        channels.put("MixHound", "UU_jxnWLGJ2eQK4en3UblKEw");
        channels.put("AirwaveMusicTV", "UUwIgPuUJXuf2nY-nKsEvLOg");
        channels.put("Proximity", "UU3ifTl5zKiCAhHIBQYcaTeg");
        channels.put("WhySoDank", "UUjE1fSNLI_RzC8-ZjkqHH9Q");
        channels.put("Liquicity", "UUSXm6c-n6lsjtyjvdD0bFVw");
        channels.put("Berzox", "UUyePQ8y0eJQ5E-EuiaE29Xg");
        channels.put("Monstafluff", "UUNqFDjYTexJDET3rPDrmJKg");
        channels.put("MajesticCastle", "UUXIyz409s7bNWVcM-vjfdVA");
        channels.put("GalaxyMusic", "UUIKF1msqN7lW9gplsifOPkQ");
        channels.put("Fluidfied", "UUTPjZ7UC8NgcZI8UKzb3rLw");
        channels.put("ArcticEmpire", "PL47GfNryB12uTUdaeDxlbBhGZQLVKJvoT");
        channels.put("eoenetwork", "UUoHJ5m7J27_u96gksCkHnlg");
        channels.put("DiversityPromotions", "UU7tD6Ifrwbiy-BoaAHEinmQ");
        channels.put("MAMusic", "UU0XKvSq8CcMBSQTKXZXnEiQ");
        channels.put("MrRevillz", "UUd3TI79UTgYvVEq5lTnJ4uQ");
        channels.put("VarietyMusic", "UUkFKSmbFIVQ1xY6j9vJlCcA");
        channels.put("Clown", "UUT4e_djPUZOkOLTZzTtnxUQ");
        channels.put("Niiiiiiiiiiii", "UUmsh_oOrl1hby7P1ZUx5Yfw");
        channels.put("MrMoMMusic", "UUJBpeNOjvbn9rRte3w_Kklg");
        channels.put("WaveMusic", "UUbuK8xxu2P_sqoMnDsoBrrg");
        channels.put("NightcoreReality", "PUqX8hO4JWM6IJfEabbZmhUw");
        channels.put("CloudKid", "UUSa8IUd1uEjlREMa21I3ZPQ");
        channels.put("TheLalaTranceGirl", "UUMQBva6MUyidoNmcV8gIV9g");
    }

    //public ArrayList<String> ids = new ArrayList<>();
    public ArrayList<String> titles = new ArrayList<>();
    public ArrayList<VideoInfo> vids = new ArrayList<>();
    int lastid = 0;
    int currentid = -1;
    int channelsDone = 0;
    int maxResults = 10;

    ListView lv;
    ArrayAdapter adapter;

    @Override
    public void onCreate() {
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setOnPreparedListener(this);
        mp.reset();
    }

    public void loadPlayList(final String playlistId, final int maxResults, boolean blockingThread) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String json = getHttp("https://www.googleapis.com/youtube/v3/playlistItems?part=contentDetails,snippet&maxResults=" + maxResults + "&playlistId=" + playlistId + "&key=" + Config.key);
                    JSONObject jsonObj = new JSONObject(json);
                    JSONArray jsonArr = jsonObj.getJSONArray("items");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject obj = jsonArr.getJSONObject(i);
                        String videoId = obj.getJSONObject("contentDetails").getString("videoId");
                        final String videoTitle = obj.getJSONObject("snippet").getString("title");
                        vids.add(new VideoInfo(videoId, videoTitle));
                        if (MainActivity.activity != null) {
                            MainActivity.activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView tv = (TextView) MainActivity.activity.findViewById(R.id.progresstext);
                                    channelsDone++;
                                    tv.setText("Loading Videos.. \n" + channelsDone + "/" + (channels.size() * maxResults));
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        if (blockingThread) {
            try {
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getHttp(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.1");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void playNext() {
        currentid++;
        if (currentid > vids.size()) {
            currentid = 0;
        }
        final iService s = this;
        final String videoId = vids.get(currentid).getId();

        try {
            URL url = new URL("https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() == 404) {
                url = new URL("https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg");
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
            }
            InputStream is = connection.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(is);
            ImageView iv = (ImageView) MainActivity.activity.findViewById(R.id.imageView);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    url = IYoutubeDownloader.download(s, videoId);

                    if (url.length() < 1) {
                        return;
                    }

                    mp.reset();

                    if (!mp.isPlaying()) {
                        try {
                            mp.setDataSource(url);

                            MainActivity.activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    lv.setSelection(currentid);
                                    if (currentid > -1 && currentid < lv.getChildCount()) {
                                        View v2 = getViewAt(lastid);
                                        if (v2 != null) {
                                            v2.setBackgroundColor(Color.TRANSPARENT);
                                        }
                                        View v = getViewAt(currentid);
                                        if (v != null) {
                                            v.setBackgroundColor(Color.parseColor("#4587ceeb"));
                                            getViewAt(currentid); // Update view by getting it
                                        }
                                    }
                                    lastid = Math.min(0, currentid);
                                    ImageButton b = (ImageButton) MainActivity.activity.findViewById(R.id.button);
                                    b.setEnabled(true);
                                    Button b2 = (Button) MainActivity.activity.findViewById(R.id.button2);
                                    b2.setEnabled(true);
                                }
                            });

                            mp.prepareAsync();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public View getViewAt(int wantedPosition) {
        int firstPosition = lv.getFirstVisiblePosition() - lv.getHeaderViewsCount();
        int wantedChild = wantedPosition - firstPosition;
        if (wantedChild < 0 || wantedChild >= lv.getChildCount()) {
            //lv.setSelection(wantedPosition);
            return null;//getViewAt(wantedPosition);
        }
        return lv.getChildAt(wantedChild);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || MainActivity.activity == null) {
            return 0;
        }

        initNotification();

        adapter = new IArrayAdapter(MainActivity.activity, R.layout.listview_item, titles);
        lv = (ListView) MainActivity.activity.findViewById(R.id.listView);
        lv.setAdapter(adapter);

        final iService s = this;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Get all videoIds
                for (String playlistId : channels.values()) {
                    System.out.println(playlistId);
                    loadPlayList(playlistId, maxResults, false);
                }

                int channelsSize = channels.size() * maxResults;
                while (channelsDone < channelsSize) {
                    // wait until all videos are loaded
                    if (channelsDone == channelsSize) {
                        break;
                    }
                }

                System.out.println("# " + vids.size());
                Collections.shuffle(vids);

                if (MainActivity.activity != null) {
                    MainActivity.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Remove loading circle
                            MainActivity.activity.findViewById(R.id.layout1).setVisibility(View.GONE);
                            // Add all loaded videos into listview
                            for (VideoInfo i : vids) {
                                titles.add(i.getTitle());
                            }
                            adapter.notifyDataSetChanged();
                            // Enable play and next button
                            ImageButton b = (ImageButton) MainActivity.activity.findViewById(R.id.button);
                            b.setEnabled(true);
                            Button b2 = (Button) MainActivity.activity.findViewById(R.id.button2);
                            b2.setEnabled(true);
                        }
                    });
                }

                s.playNext();

            }
        });
        thread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        System.out.println("Removing notifications.");
        cancelNotification(true);

        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
        }

        cancelNotification(false);
    }


    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        System.out.println("## > Mediaplayer onError.");
        playNext();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer arg0) {
        System.out.println("## > Mediaplayer onPrepared.");
        if (!mp.isPlaying()) {
            mp.start();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        System.out.println("## > Mediaplayer onCompletion.");
        if (this.mp.isPlaying()) {
            this.mp.stop();
        }
        playNext();
    }

    public class LocalBinder extends Binder {
        iService getService() {
            return iService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // I have no idea where I got this from and whether this is needed or not.
    public void initNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.abc_btn_radio_material;
        CharSequence tickerText = "InstanceRadio";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        Context context = getApplicationContext();
        CharSequence contentTitle = "InstanceRadio";
        CharSequence contentText = "Playing music right now.";
        Intent notificationIntent = MainActivity.activity.getIntent();
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notification.deleteIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        mNotificationManager.notify(1, notification);
    }

    public void cancelNotification(boolean all) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        if (all) {
            mNotificationManager.cancelAll();
        }
        mNotificationManager.cancel(1);
    }

}
