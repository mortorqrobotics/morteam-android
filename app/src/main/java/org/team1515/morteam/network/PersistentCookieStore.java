package org.team1515.morteam.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.HTTP;
import org.team1515.morteam.MorTeam;

import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ariel on 9/15/17.
 */

public class PersistentCookieStore implements CookieStore {
    private final String TAG = "PersistentCookieStore";
    private final Type type;

    private boolean foundCookies;

    private Map<URI, List<HttpCookie>> cookies;
    private Gson gson;

    @SuppressWarnings("unchecked")
    public PersistentCookieStore() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(HttpCookie.class, new HttpCookieSerializer());
        builder.registerTypeAdapter(HttpCookie.class, new HttpCookieDeserializer());

        gson = builder.create();
        cookies = new HashMap<>();
        type = new TypeToken<ArrayList<HttpCookie>>() {}.getType();

        // Populate HashMap with contents of SharedPrefs with cookies
        String cookieStr = MorTeam.cookieSerialize.getString("cookies", "");
        if (cookieStr.equals("")) {
            this.foundCookies = false;
            return;
        }

        for (HttpCookie cookie : (ArrayList<HttpCookie>) gson.fromJson(cookieStr, type)) {
            if (cookie.hasExpired())
                continue;

            try {
                Log.v(TAG, cookie.getCommentURL());
                this.addCookie(new URI(cookie.getCommentURL()), cookie);
            } catch (URISyntaxException e) {
                // this is bad, and i feel bad for writing it.
                Log.e(TAG, "Invalid URI syntax when attempting to deserialize cookies, serialized store invalidated", e);
                this.foundCookies = false;
                return;
            }
        }

        Log.v(TAG, "Retrieved " + Integer.toString(cookies.size()) + " cookies");
        this.foundCookies = true;
    }

    public void add(URI uri, HttpCookie cookie) {
        if (cookie == null)
            throw new NullPointerException("Attempted to add a null cookie");
        // Make sure cookie has not expired
        if (cookie.hasExpired())
            return;

        // Add cookie to hashmap, then write newly serialized array of cookies to SharedPrefs to make sure we don't lose any data
        // what follows is a hack to make serializing the cookie uri easier. please refrain from judgement, it's 11:52
        Log.v(TAG, "Adding cookie");
        Log.v(TAG, cookie.getName());
        Log.v(TAG, cookie.toString());
        cookie.setCommentURL(uri == null ? "" : uri.toString());
        this.addCookie(uri, cookie);
        this.serialize();
    }

    public List<HttpCookie> get(URI uri) {
        Log.v(TAG, "Returning cookie(s) for host " + uri.getHost());
        Log.v(TAG, "Exact uri: " + uri.toString());
        ArrayList<HttpCookie> toReturn = new ArrayList<>();

        if (uri == null)
            throw new NullPointerException("Attempted to retrieve cookie from null uri");

        if (cookies.get(uri) == null) {
            for (URI key : cookies.keySet()) {
                if (key.getHost().equals(uri.getHost())) {
                    Log.v(TAG, "special");
                    toReturn.addAll(cookies.get(key));
                }
            }

        } else {
            Log.v(TAG, "else");
            toReturn.addAll(cookies.get(uri));
        }
        
        return toReturn;
    }

    public List<HttpCookie> getCookies() {
        List<HttpCookie> result = new ArrayList<>();

        for (List<HttpCookie> value : cookies.values()) {
            for (HttpCookie cookie : value) {
                if (cookie.hasExpired()) {
                    value.remove(cookie);
                }
            }

            result.addAll(value);
        }

        return result;
    }

    public List<URI> getURIs() {
        // Apparently this is the most efficient way to convert a list to a set
        List<URI> result = new ArrayList<>();
        result.addAll(cookies.keySet());

        return result;
    }

    public boolean remove(URI uri, HttpCookie cookie) {
        if (cookie == null)
            throw new NullPointerException("Attempted to remove a null cookie");

        boolean success = cookies.get(uri).remove(cookie);
        if (cookies.get(uri).size() == 0)
            cookies.remove(uri);

        // since we want to be sure that the serialized contents matches the map
        if (success) {
            this.serialize();
        }

        return success;
    }

    public boolean removeAll() {
        cookies.clear();
        MorTeam.cookieSerialize.edit().remove("cookies").apply();
        Log.v(TAG, "clearing cookies");

        return true;
    }

    public boolean foundCookies() {
        return this.foundCookies;
    }

    private void addCookie(URI uri, HttpCookie cookie) {
        if (cookies.containsKey(uri))
            cookies.get(uri).add(cookie);
        else {
            ArrayList<HttpCookie> currentUri = new ArrayList<>();
            currentUri.add(cookie);
            cookies.put(uri, currentUri);
        }
    }

    private void serialize() {
        ArrayList<HttpCookie> masterList = new ArrayList<>();
        for (List<HttpCookie> value : cookies.values()) {
            masterList.addAll(value);
        }

        MorTeam.cookieSerialize.edit().putString("cookies", gson.toJson(masterList, type)).apply();
    }
}
