package org.team1515.morteam.network;

public class NetworkUtils {
    // Constants
    public static final String HOST = "http://www.morteam.com";
    public static final String PATH_PREFIX = "/api";
    public static final int PORT = 8080;

    public static final String SET_COOKIE_KEY = "set-cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SESSION_COOKIE = "connect.sid";

    public static String makeURL(String path, boolean usePrefix) {
        return HOST + ":" + PORT + (usePrefix ? PATH_PREFIX : "") + path;
    }
}
