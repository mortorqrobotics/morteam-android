package net.team1515.morganizer.network;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 6/11/15.
 */
public class Connection {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost("127.0.0.1:8080/loginUser");

    List<BasicNameValuePair> pairs = new ArrayList<>();

}