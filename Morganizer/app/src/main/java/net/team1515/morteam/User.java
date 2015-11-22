package net.team1515.morteam;

public class User {
    public String firstName;
    public String lastName;
    public String id;

    public User(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }
}
