package org.team1515.morteam.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.net.HttpCookie;

/**
 * Created by ariel on 6/21/18.
 */

public class HttpCookieSerializer implements JsonSerializer<HttpCookie> {
    @Override
    public JsonElement serialize(final HttpCookie cookie, final Type type, final JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", cookie.getName());
        jsonObject.addProperty("value", cookie.getValue());
        jsonObject.addProperty("domain", cookie.getDomain());
        jsonObject.addProperty("path", cookie.getPath());
        jsonObject.addProperty("commentURL", cookie.getCommentURL());
        jsonObject.addProperty("maxAge", cookie.getMaxAge());

        return jsonObject;
    }
}
