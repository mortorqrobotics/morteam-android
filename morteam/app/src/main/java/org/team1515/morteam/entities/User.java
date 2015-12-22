package org.team1515.morteam.entities;

public class User {
    private String firstName;
    private String lastName;
    private String id;
    private String profPicPath;
    private String email;

    public User(String firstName, String lastName, String id, String profPicPath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.profPicPath = profPicPath;
        this.email = "";
    }

    public User(String firstName, String lastName, String profPicPath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = "";
        this.profPicPath = profPicPath;
        this.email = "";
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getId() {
        return id;
    }

    public String getProfPicPath() {
        return profPicPath;
    }
}
