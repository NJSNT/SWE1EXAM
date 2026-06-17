package model;

import client.NetworkClient;
import network.Request;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class ClientModel {
    private NetworkClient networkClient;
    private User currentUser;
    private PropertyChangeSupport support;
    private List<Notification> notificationHistory;

    public ClientModel() {
        this.support = new PropertyChangeSupport(this);
        this.notificationHistory = new ArrayList<>();
        this.networkClient = new NetworkClient(this);
    }

    public void start() {
        networkClient.connect();
    }

    public void login(String username, String password) {
        String[] credentials = {username, password};
        networkClient.sendRequest(new Request("LOGIN", credentials));
    }

    public void registerStudent(String username, String password) {
        User newUser = new Student(username, password);
        networkClient.sendRequest(new Request("REGISTER", newUser));
    }

    public void fetchCourses() {
        networkClient.sendRequest(new Request("GET_COURSES", null));
    }

    public void fetchTags() {
        networkClient.sendRequest(new Request("GET_TAGS", null));
    }

    public void fetchUsers() {
        networkClient.sendRequest(new Request("GET_USERS", null));
    }

    public void updateCurrentUserTags(String tags) {
        currentUser.setTags(tags);
        networkClient.sendRequest(new Request("UPDATE_USER_TAGS", currentUser));
    }

    public void addCourse(Course course) {
        networkClient.sendRequest(new Request("ADD_COURSE", course));
    }

    public void deleteCourse(Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("DELETE_COURSE", payload));
    }

    public void adminDeleteCourse(Course course) {
        networkClient.sendRequest(new Request("ADMIN_DELETE_COURSE", course));
    }

    public void adminDeleteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_DELETE_USER", user));
    }

    public void adminPromoteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_PROMOTE_USER", user));
    }

    public void adminDemoteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_DEMOTE_USER", user));
    }

    public void updateCourse(Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("UPDATE_COURSE", payload));
    }

    public void enrollCourse(Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("ENROLL_COURSE", payload));
    }

    public void fetchRegisteredCourses() {
        networkClient.sendRequest(new Request("GET_REGISTERED_COURSES", currentUser));
    }

    public void cancelRegistration(Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("CANCEL_REGISTRATION", payload));
    }

    public void fetchDirectChatHistory(User chatPartner) {
        Object[] payload = {currentUser, chatPartner};
        networkClient.sendRequest(new Request("GET_DIRECT_CHAT_HISTORY", payload));
    }

    public void fetchChatPartners() {
        networkClient.sendRequest(new Request("GET_CHAT_PARTNERS", currentUser));
    }

    public void sendDirectMessage(User receiver, String text) {
        Message msg = new Message(currentUser, receiver, text);
        networkClient.sendRequest(new Request("SEND_DIRECT_MESSAGE", msg));
    }

    public void logout() {
        currentUser = null;
        // Clear local history only — the server DB records stay intact so
        // notifications appear again on the next login if not yet dismissed.
        synchronized (this) {
            notificationHistory.clear();
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void receiveNotification(Notification notification) {
        synchronized (this) {
            notificationHistory.add(notification);
        }
        fireEvent("NewNotification", null, notification);
    }

    public synchronized List<Notification> getNotificationHistory() {
        return new ArrayList<>(notificationHistory);
    }

    public synchronized int getUnreadNotificationCount() {
        int count = 0;
        for (Notification notification : notificationHistory) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    public void markAllNotificationsRead() {
        synchronized (this) {
            for (Notification notification : notificationHistory) {
                notification.setRead(true);
            }
        }
        fireEvent("NotificationsRead", null, null);
    }

    public void markNotificationsFromUserRead(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return;
        }
        synchronized (this) {
            for (Notification notification : notificationHistory) {
                if (userName.equals(notification.getRelatedUserName())) {
                    notification.setRead(true);
                }
            }
        }
        fireEvent("NotificationsRead", null, null);
    }

    /**
     * Removes a notification from local memory and tells the server to delete
     * it from the DB so it doesn't reappear on the next login.
     */
    public void removeNotification(Notification notification) {
        if (notification == null) {
            return;
        }
        synchronized (this) {
            notificationHistory.remove(notification);
        }
        fireEvent("NotificationRemoved", null, notification);

        // Tell the server to permanently delete this notification row.
        if (notification.getId() != -1) {
            networkClient.sendRequest(new Request("DISMISS_NOTIFICATION", notification.getId()));
        }
    }

    /**
     * Clears all notifications from local memory and tells the server to delete
     * all DB records for this user so they don't reappear on the next login.
     */
    public void clearNotifications() {
        synchronized (this) {
            notificationHistory.clear();
        }
        fireEvent("NotificationsCleared", null, null);

        // Tell the server to wipe all persistent notification rows for this user.
        networkClient.sendRequest(new Request("CLEAR_NOTIFICATIONS", null));
    }

    public void addListener(String eventName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(eventName, listener);
    }

    public void removeListener(String eventName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(eventName, listener);
    }

    public void fireEvent(String eventName, Object oldValue, Object newValue) {
        support.firePropertyChange(eventName, oldValue, newValue);
    }
}
