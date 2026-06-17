package viewmodel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Notification;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NotificationViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<Notification> notifications;

    public NotificationViewModel(ClientModel model) {
        this.model = model;
        this.notifications = FXCollections.observableArrayList();

        // Load notifications that arrived before this window was opened.
        this.notifications.addAll(model.getNotificationHistory());

        this.model.addListener("NewNotification", this);
        this.model.addListener("NotificationsRead", this);
        this.model.addListener("NotificationRemoved", this);
        this.model.addListener("NotificationsCleared", this);
    }

    public ObservableList<Notification> getNotifications() {
        return notifications;
    }

    public ClientModel getModel() {
        return model;
    }

    public void clearNotifications() {
        model.clearNotifications();
    }

    public void dispose() {
        model.removeListener("NewNotification", this);
        model.removeListener("NotificationsRead", this);
        model.removeListener("NotificationRemoved", this);
        model.removeListener("NotificationsCleared", this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("NewNotification".equals(evt.getPropertyName())) {
                Notification notification = (Notification) evt.getNewValue();
                notifications.add(notification);
            } else if ("NotificationsRead".equals(evt.getPropertyName())) {
                notifications.setAll(model.getNotificationHistory());
            } else if ("NotificationRemoved".equals(evt.getPropertyName())) {
                Notification notification = (Notification) evt.getNewValue();
                notifications.remove(notification);
            } else if ("NotificationsCleared".equals(evt.getPropertyName())) {
                notifications.clear();
            }
        });
    }

    public void removeNotification(Notification notification) {
        model.removeNotification(notification);
    }
}
