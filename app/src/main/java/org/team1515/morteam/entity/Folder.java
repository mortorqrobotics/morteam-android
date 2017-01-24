package org.team1515.morteam.entity;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Folder {
    private String id;
    private Date created_at;
    private Date updated_at;
    private String name;
    private boolean defaultFolder;
    private User creator;

    //Not sure what else we need

    public Folder(String id, String created_at, String updated_at, String name, boolean defaultFolder, User creator) {
        this.id = id;
        try {
            this.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(created_at.replace("Z", "+0000"));
            this.updated_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(updated_at.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.name = name;
        this.defaultFolder = defaultFolder;
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public String getCreationDate() {
        return DateFormat.format("MMM d, h:mm a", created_at).toString();
    }

    public String getUpdateDate() {
        return DateFormat.format("MMM d, h:mm a", updated_at).toString();
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultFolder() {
        return defaultFolder;
    }

    public User getCreator() {
        return creator;
    }
}
