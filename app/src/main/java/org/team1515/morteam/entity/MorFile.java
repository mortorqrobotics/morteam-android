package org.team1515.morteam.entity;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MorFile {
    private String id;
    private Date created_at;
    private Date updated_at;
    private String name;
    private String originalName;
    private int size;
    private String type;
    private String mimeType;
    private User creator;
    private String folderName;

    public MorFile(String id, String created_at, String updated_at, String name, String originalName, int size, String type, String mimeType, String creator, String folderName) {
        this.id = id;
        try {
            this.created_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(created_at.replace("Z", "+0000"));
            this.updated_at = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(updated_at.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.name = name;
        this.originalName = originalName;
        this.size = size;
        this.type = type;
        this.mimeType = mimeType;
        this.creator = new User(creator);
        this.folderName = folderName;
    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return DateFormat.format("MMM d, h:mm a", created_at).toString();
    }

    public String getUpdated_at() {
        return DateFormat.format("MMM d, h:mm a", updated_at).toString();
    }

    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getMimeType() {
        return mimeType;
    }

    public User getCreator() {
        return creator;
    }

    public String getFolderName() {
        return folderName;
    }
}