package org.team1515.morteam.entities;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task {
    private User assigner;
    private String id;
    private Date dueDate;
    private String title;
    private String description;

    public Task(User assigner, String id, String dueDate, String title, String description) {
        this.assigner = assigner;
        this.id = id;
        try {
            this.dueDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(dueDate.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
            dueDate = null;
        }
        this.title = title;
        this.description = description;
    }

    public String getAssignerName() {
        return assigner.getFullName();
    }

    public String getAssignerId() {
        return assigner.getId();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return DateFormat.format("MMMM d, yyyy", dueDate).toString();
    }

}
