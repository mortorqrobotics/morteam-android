package org.team1515.morteam.entity;

public class Subdivision {
    private String name;
    private String id;

    public Subdivision(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Subdivision(String id) {
        this("", id);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
