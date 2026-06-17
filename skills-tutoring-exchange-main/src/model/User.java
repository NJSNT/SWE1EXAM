package model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public abstract class User implements Serializable {
    private String name;
    private String password;
    private String tags;
    private List<Notification> notifications;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.tags = "";
        this.notifications = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        // User tags are stored as simple text, like "java, sql, oop"
        if (tags == null) {
            this.tags = "";
        } else {
            this.tags = tags.trim();
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    // Method to receive a notification, matching the "receives" relationship
    public void receiveNotification(Notification notification) {
        this.notifications.add(notification);
    }

    @Override
    public String toString() {
        return name;
    }
}
