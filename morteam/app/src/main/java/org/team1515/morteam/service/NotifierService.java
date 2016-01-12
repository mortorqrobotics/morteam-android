package org.team1515.morteam.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.activity.MainActivity;
import org.team1515.morteam.entities.Announcement;
import org.team1515.morteam.entities.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.List;

public class NotifierService extends IntentService {
    private RequestQueue queue;
    private SharedPreferences preferences;

    private PowerManager.WakeLock wakeLock;

    public NotifierService() {
        super("MorTeam Notifier");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //Hopefully stop crashes? Need better solution
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "morteam");
            wakeLock.acquire();

            // check the global background data setting
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (!connectivityManager.getBackgroundDataSetting()) {
                stopSelf();
                return;
            }

            preferences = getSharedPreferences(null, 0);
            queue = Volley.newRequestQueue(this);
            final Gson gson = new Gson();


            CookieRequest announcementsRequest = new CookieRequest(Request.Method.POST, "/f/getAnnouncementsForUser", preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        //Transfer json into announcement array
                        JSONArray announcementArray = new JSONArray(response);
                        ArrayList<Announcement> serverAnnouncements = new ArrayList<>();
                        for (int i = 0; i < announcementArray.length(); i++) {
                            JSONObject announcementObject = announcementArray.getJSONObject(i);
                            serverAnnouncements.add(new Announcement(
                                    new User(announcementObject.getJSONObject("author").getString("firstname"),
                                            announcementObject.getJSONObject("author").getString("lastname"),
                                            announcementObject.getJSONObject("author").getString("profpicpath")),
                                    announcementObject.getString("content"),
                                    announcementObject.getString("timestamp"),
                                    announcementObject.getString("_id")
                            ));
                        }

                        //Get local announcements in json and convert to object list
                        List<Announcement> localAnnouncements = null;
                        String localJsonAnnouncements = preferences.getString("announcements", null);
                        if (localJsonAnnouncements != null) {
                            localAnnouncements = gson.fromJson(localJsonAnnouncements, new TypeToken<ArrayList<Announcement>>() {
                            }.getType());

                            //Check if new announcements are found
                            List<Announcement> newAnnouncements;
                            if (localAnnouncements.isEmpty()) {
                                newAnnouncements = serverAnnouncements;
                            } else {
                                newAnnouncements = new ArrayList<>();
                                for (Announcement announcement : serverAnnouncements) {
                                    if (!announcement.getDate().equals(localAnnouncements.get(0).getDate()) && announcement.getRawDate().after(localAnnouncements.get(0).getRawDate())) {
                                        newAnnouncements.add(announcement);
                                    }
                                }
                            }

                            //Create notification with new announcements
                            if (!newAnnouncements.isEmpty()) {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(NotifierService.this);
                                builder.setSmallIcon(R.drawable.ic_floating_button);
                                builder.setPriority(0);
                                builder.setAutoCancel(true);
                                builder.setDefaults(Notification.DEFAULT_SOUND);

                                if (newAnnouncements.size() == 1) {
                                    builder.setContentText(Html.fromHtml(newAnnouncements.get(0).getContent()));
                                    builder.setContentTitle("New Announcement");
                                } else {
                                    builder.setContentTitle("New Announcements");
                                    builder.setContentText(newAnnouncements.size() + " " + "Announcements");
                                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                                    inboxStyle.setBigContentTitle("New Announcements");
                                    for (Announcement announcement : newAnnouncements) {
                                        inboxStyle.addLine(Html.fromHtml(announcement.getContent()));
                                    }
                                    builder.setStyle(inboxStyle);
                                }

                                Intent resultIntent = new Intent(NotifierService.this, MainActivity.class);
                                TaskStackBuilder stackBuilder = TaskStackBuilder.create(NotifierService.this);
                                stackBuilder.addParentStack(MainActivity.class);
                                stackBuilder.addNextIntent(resultIntent);
                                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(resultPendingIntent);
                                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(0, builder.build());
                            }
                        }

                        //Update local copy of announcements with server copy
                        preferences.edit().putString("announcements", gson.toJson(serverAnnouncements)).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if(wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                    wakeLock.release();
                }
            });
            queue.add(announcementsRequest);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
