package org.team1515.morteam.entity;

import android.content.SharedPreferences;
import android.text.format.DateFormat;

import com.android.volley.RequestQueue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Event {
    private User creator;
    private String id;
    private String title;
    private String description;
    private Date date;
    private List<User> userAttendees;
    private List<Subdivision> subdivisionAttendees;
    private boolean hasAttendance;

    public Event(User creator, String id, String title, String description, String date,
                 List<User> userAttendees, List<Subdivision> subdivisionAttendees) {
        this.creator = creator;
        this.id = id;
        this.title = title;
        this.description = description;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(date.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        this.userAttendees = userAttendees;
        this.subdivisionAttendees = subdivisionAttendees;
    }

    public String getCreatorName() {
        return creator.getFullName();
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

    public String getDate() {
        return DateFormat.format("h:mm a - MMMM d, yyyy", date).toString();
    }

    public int getDay() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH) - 1;
    }

    public void populateUser(SharedPreferences preferences, RequestQueue queue) {
        creator.populate(preferences, queue, false);
    }
}
