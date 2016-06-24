package org.team1515.morteam.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.team1515.morteam.R;

import java.io.IOException;

public class RegistrationService extends IntentService {

    public RegistrationService() {
        super("Registration Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = getSharedPreferences(null, 0);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            //TODO: send registration to server
            Log.d("MorTeam Registration", token);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
