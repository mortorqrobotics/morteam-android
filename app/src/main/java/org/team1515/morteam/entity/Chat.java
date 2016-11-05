package org.team1515.morteam.entity;

public class Chat {
    private String name;
    private String id;
    private String picPath;
    public boolean isGroup;

    public Chat(String name, String id, String picPath, boolean isGroup) {
        this.name = name;
        this.id = id;
        this.picPath = picPath;
        this.isGroup = isGroup;
        if (picPath.substring(0, 3).equals("/pp")) {
            this.picPath = "http://profilepics.morteam.com.s3.amazonaws.com" + picPath.substring(3);
        } else {
            this.picPath = "http://www.morteam.com:8080" + picPath;
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPicPath() {
        return picPath;
    }
}
