package org.team1515.morteam.network;

import com.google.gson.Gson;

import org.team1515.morteam.MorTeam;

import java.io.ObjectOutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ariel on 9/15/17.
 */

public class PersistentCookieStore implements CookieStore {
    private Map<URI, List<HttpCookie>> cookies;
    private ObjectOutputStream mapSerializer;
    private Gson gson;

    @SuppressWarnings("unchecked")
    public PersistentCookieStore() {
        cookies = new HashMap<>();
        gson = new Gson();

        // Populate HashMap with contents of SharedPrefs with cookies
        for (String gsonStr : MorTeam.cookiePrefs.getAll().keySet()) {
            URI key = gson.fromJson(gsonStr, URI.class);
            List<HttpCookie> value = gson.fromJson(MorTeam.cookiePrefs.getString(gsonStr, "DEFAULT"), List.class);

            // Make sure all cookies have not expired
            for (HttpCookie cookie : value) {
                if (cookie.hasExpired())
                    value.remove(cookie);
            }

            cookies.put(key, value);
        }
    }

    public void add(URI uri, HttpCookie cookie) {
        // Add cookies to HashMap, then to SharedPreferences

        // Make sure cookie has not expired
        if (cookie.hasExpired())
            return;

        if (cookies.get(uri) == null) {
            cookies.put(uri, Arrays.asList(cookie));
        } else {
            cookies.get(uri).add(cookie);
        }

        MorTeam.cookiePrefs.edit().putString(gson.toJson(uri), gson.toJson(cookies.get(uri))).apply();
    }

    public List<HttpCookie> get(URI uri) {
        return cookies.get(uri);
    }

    public List<HttpCookie> getCookies() {
        List<HttpCookie> result = new ArrayList<>();

        for (List<HttpCookie> value : cookies.values()) {
            for (HttpCookie cookie : value) {
                if (cookie.hasExpired())
                    value.remove(cookie);
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
        boolean success = cookies.get(uri).remove(cookie);

        // since we want to be sure that the contents of the map matches that of the SharedPrefs
        if (success) {
            MorTeam.preferences.edit().putString(gson.toJson(uri), gson.toJson(cookies.get(uri))).apply();
        }

        return success;
    }

    public boolean removeAll() {
        cookies.clear();
        MorTeam.preferences.edit().clear().apply();

        return true;
    }
}
