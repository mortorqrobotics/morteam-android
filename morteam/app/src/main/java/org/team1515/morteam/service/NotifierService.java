package org.team1515.morteam.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import net.team1515.morteam.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.activity.ChatActivity;
import org.team1515.morteam.network.CookieRequest;

import java.net.URISyntaxException;
import java.util.Map;

public class NotifierService extends IntentService {
    private RequestQueue queue;
    private SharedPreferences preferences;

    private Socket socket;

    public NotifierService() {
        super("MorTeam Notifier");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        queue = Volley.newRequestQueue(this);
        preferences = getSharedPreferences(null, 0);

        try {
            final String sessionId = preferences.getString(CookieRequest.SESSION_COOKIE, "");
            socket = IO.socket("http://www.morteam.com");
            socket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Transport transport = (Transport) args[0];
                    transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> headers = (Map<String, String>) args[0];

                            // set header
                            //Insert session-id cookie into header
                            if (sessionId.length() > 0) {
                                StringBuilder builder = new StringBuilder();
                                builder.append(CookieRequest.SESSION_COOKIE);
                                builder.append("=");
                                builder.append(sessionId);
                                if (headers.containsKey(CookieRequest.COOKIE_KEY)) {
                                    builder.append("; ");
                                    builder.append(headers.get(CookieRequest.COOKIE_KEY));
                                }
                                headers.put(CookieRequest.COOKIE_KEY, builder.toString());
                            }
                        }
                    }).on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> headers = (Map<String, String>) args[0];
                            //No headers to get here at the moment
                        }
                    });
                }
            });
            socket = socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject messageObject = new JSONObject(args[0].toString());

                    final String name = messageObject.getString("author_fn") + " " + messageObject.getString("author_ln");
                    final String content = messageObject.getString("content");
                    final String chatId = messageObject.getString("chat_id");
                    final String profPicPath = messageObject.getString("author_profpicpath");

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(NotifierService.this);
                    builder.setSmallIcon(R.mipmap.ic_launcher);
                    builder.setContentTitle("New Message");
                    builder.setContentText(content);

                    Intent notificationIntent = new Intent(NotifierService.this, ChatActivity.class);
                    notificationIntent.putExtra("name", name);
                    notificationIntent.putExtra("_id", chatId);
                    notificationIntent.putExtra("isGroup", false);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotifierService.this);
                    stackBuilder.addParentStack(ChatActivity.class);
                    stackBuilder.addNextIntent(notificationIntent);

                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());


                    Intent intent = new Intent("message");
                    intent.putExtra("name", name);
                    intent.putExtra("content", content);
                    intent.putExtra("chatId", chatId);
                    intent.putExtra("profPicPath", profPicPath);
                    LocalBroadcastManager.getInstance(NotifierService.this).sendBroadcast(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        System.out.println("Created service");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        System.out.println("STARTED");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("STOPPED");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("Bound service");
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
