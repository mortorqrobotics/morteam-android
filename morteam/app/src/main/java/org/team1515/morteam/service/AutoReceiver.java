package org.team1515.morteam.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NotifierService.class);
        context.startService(serviceIntent);
    }
}
