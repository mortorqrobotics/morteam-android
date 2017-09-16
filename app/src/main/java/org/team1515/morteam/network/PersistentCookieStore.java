package org.team1515.morteam.network;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by ariel on 9/15/17.
 */

public class PersistentCookieStore implements CookieStore {
    private Map<URI, HttpCookie> cookies;

    public void add(URI uri, HttpCookie cookie) {
        
    }

    public List<HttpCookie> get(URI uri) {

    }

    public List<HttpCookie> getCookies() {

    }

    public List<URI> getURIs() {

    }

    public boolean remove(URI uri, HttpCookie cookie) {

    }

    public boolean removeAll() {

    }
}
