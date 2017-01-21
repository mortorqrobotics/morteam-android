package org.team1515.morteam.entity;

import java.util.Date;

public class Folder {
    private String name;
    private boolean defaultFolder;
    private User creator;

    //Not sure what else we need

    public Folder(String name, boolean defaultFolder, User creator) {
        this.name = name;
        this.defaultFolder = defaultFolder;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultFolder() {
        return defaultFolder;
    }

    public void setDefaultFolder(boolean defaultFolder) {
        this.defaultFolder = defaultFolder;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}
