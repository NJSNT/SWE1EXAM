package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Notification;
import model.Student;
import model.User;
import viewmodel.NotificationViewModel;

import java.time.format.DateTimeFormatter;

public class NotificationViewController {

    @FXML private ListView<Notification> notificationListView;
    @FXML private Button clearButton;

    private NotificationViewModel viewModel;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void init(NotificationViewModel viewModel) {
        this.viewModel = viewModel;

        notificationListView.setItems(viewModel.getNotifications());
        notificationListView.setCellFactory(listView -> createNotificationCell());
        clearButton.disableProperty().bind(javafx.beans.binding.Bindings.isEmpty(viewModel.getNotifications()));
        notificationListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openChatFromSelectedNotification();
            }
        });

        // Keep the newest notification visible when several messages arrive.
        viewModel.getNotifications().addListener((javafx.collections.ListChangeListener.Change<? extends Notification> c) -> {
            notificationListView.scrollTo(viewModel.getNotifications().size() - 1);
        });
    }

    @FXML
    public void onClearButton() {
        viewModel.clearNotifications();
    }

    @FXML
    public void onCloseButton() {
        Stage stage = (Stage) notificationListView.getScene().getWindow();
        stage.close();
    }

    private void openChatFromSelectedNotification() {
        Notification selectedNotification = notificationListView.getSelectionModel().getSelectedItem();
        if (selectedNotification == null || selectedNotification.getRelatedUserName().isEmpty()) {
            return;
        }

        User chatPartner = new Student(selectedNotification.getRelatedUserName(), "");

        ChatWindowManager.openChat(viewModel.getModel(), chatPartner);
        // The selected notification is handled after it opens the related chat.
        viewModel.removeNotification(selectedNotification);
    }

    private ListCell<Notification> createNotificationCell() {
        return new ListCell<>() {
            private final VBox content = new VBox(4);
            private final Label titleLabel = new Label();
            private final Label messageLabel = new Label();
            private final Label timeLabel = new Label();

            {
                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                messageLabel.setStyle("-fx-text-fill: #2f2f2f;");
                timeLabel.setStyle("-fx-text-fill: #7f8c8d;");

                titleLabel.setWrapText(true);
                messageLabel.setWrapText(true);

                // Keep long notification text inside the list.
                content.maxWidthProperty().bind(notificationListView.widthProperty().subtract(24));
                titleLabel.maxWidthProperty().bind(notificationListView.widthProperty().subtract(40));
                messageLabel.maxWidthProperty().bind(notificationListView.widthProperty().subtract(40));

                content.getChildren().addAll(titleLabel, messageLabel, timeLabel);
                content.setPadding(new Insets(6, 8, 6, 8));
            }

            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);

                if (empty || notification == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                titleLabel.setText(safeText(notification.getTitle()));
                messageLabel.setText(safeText(notification.getMessageInformation()));
                if (notification.getCreatedAt() == null) {
                    timeLabel.setText("");
                } else {
                    timeLabel.setText(notification.getCreatedAt().format(timeFormatter));
                }

                setText(null);
                setGraphic(content);
            }
        };
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }
}
