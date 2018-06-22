package org.team1515.morteam.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.net.HttpCookie;

/**
 * Created by ariel on 6/21/18.
 */

public class HttpCookieDeserializer implements JsonDeserializer<HttpCookie> {
    public HttpCookie deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
        JsonObject object = jsonElement.getAsJsonObject();

        HttpCookie cookie = new HttpCookie(object.get("name").getAsString(), object.get("value").getAsString());
        cookie.setDomain(object.get("domain").getAsString());
        cookie.setPath(object.get("path").getAsString());
        cookie.setCommentURL(object.get("commentURL").getAsString());
        cookie.setMaxAge(object.get("maxAge").getAsLong());

        return cookie;
    }
}
