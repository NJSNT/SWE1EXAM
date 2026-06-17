package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    // DB primary key. -1 means the notification hasn't been persisted yet.
    private int id = -1;
    private String title;
    private String messageInformation;
    private String relatedUserName;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification(String title, String messageInformation) {
        this(title, messageInformation, "");
    }

    public Notification(String title, String messageInformation, String relatedUserName) {
        this.title = title;
        this.messageInformation = messageInformation;
        setRelatedUserName(relatedUserName);
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessageInformation() {
        return messageInformation;
    }

    public void setMessageInformation(String messageInformation) {
        this.messageInformation = messageInformation;
    }

    public String getRelatedUserName() {
        return relatedUserName;
    }

    public void setRelatedUserName(String relatedUserName) {
        if (relatedUserName == null) {
            this.relatedUserName = "";
        } else {
            this.relatedUserName = relatedUserName.trim();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return title + ": " + messageInformation;
    }
}
