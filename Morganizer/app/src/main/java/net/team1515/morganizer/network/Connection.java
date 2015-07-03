package net.team1515.morganizer.network;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import java.util.jar.Attributes;

/**
 * Created by david on 6/11/15.
 */
public class Connection extends AsyncTask<NameValuePair, Void, String> {
    URL url;
    HttpURLConnection http;

    public Connection(String host, String file) {
        try {
            url = new URL("http", host, file);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        @Override
    protected String doInBackground(NameValuePair... pairs) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();

            String params = "{";
            for(NameValuePair pair : pairs) {
                params += pair.getName() + ":" + "'" + pair.getValue() + "',";
            }
            params = params.substring(0, params.length() - 1) + "}";
            byte[] postData = params.getBytes();
            int postDataLength = postData.length;

            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setFixedLengthStreamingMode(params.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(params);
            out.close();

            String response = "";
            Scanner inStream = new Scanner(connection.getInputStream());
            while(inStream.hasNextLine()) {
                response += inStream.nextLine();
            }


            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error";
        }
    }
}