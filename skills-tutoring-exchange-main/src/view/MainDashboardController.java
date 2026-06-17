package view;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import viewmodel.ChatHistoryViewModel;
import viewmodel.DashboardViewModel;
import viewmodel.NotificationViewModel;
import viewmodel.RegistrationViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class MainDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<model.Course> courseListView;
    // Filter area
    @FXML private ComboBox<String> userTagComboBox;
    @FXML private FlowPane userTagsChipPane;
    // Search
    @FXML private TextField searchField;
    // Add Course form — identical to original
    @FXML private TextField courseNameField;
    @FXML private TextField courseTagsField;
    @FXML private TextArea courseInfoField;
    // Buttons
    @FXML private Button deleteCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button enrollCourseButton;
    @FXML private Button notificationsButton;
    @FXML private Label statusLabel;

    private DashboardViewModel viewModel;
    private Runnable onLogout;
    private Stage notificationStage;

    public void init(DashboardViewModel viewModel) {
        this.viewModel = viewModel;

        // ── Basic bindings (unchanged from original) ───────────────────────────
        welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());
        courseNameField.textProperty().bindBidirectional(viewModel.newCourseNameProperty());
        courseInfoField.textProperty().bindBidirectional(viewModel.newCourseInfoProperty());
        // Course tags stays a free-text field — same bidirectional binding as original.
        courseTagsField.textProperty().bindBidirectional(viewModel.newCourseTagsProperty());
        notificationsButton.textProperty().bind(viewModel.notificationsButtonTextProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // ── Course list ────────────────────────────────────────────────────────
        courseListView.setItems(viewModel.getCourseList());
        courseListView.setCellFactory(lv -> createCourseCell());
        courseListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, now) -> viewModel.setSelectedCourse(now));

        deleteCourseButton.disableProperty().bind(viewModel.canDeleteSelectedCourseProperty().not());
        editCourseButton.disableProperty().bind(viewModel.canEditSelectedCourseProperty().not());
        enrollCourseButton.disableProperty().bind(viewModel.canEnrollSelectedCourseProperty().not());

        viewModel.setOnEnrollmentSuccess(() -> openTutorChat(viewModel.getLastEnrolledCourse()));

        // ── Filter tag dropdown ────────────────────────────────────────────────
        // Populated from availableTags (fetched from server on startup).
        userTagComboBox.setItems(viewModel.getAvailableTags());
        userTagComboBox.setOnAction(e -> {
            String selected = userTagComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewModel.addUserTag(selected);
                // Clear the selection so the same tag can be picked again after removal.
                Platform.runLater(() -> userTagComboBox.getSelectionModel().clearSelection());
            }
        });

        // Rebuild chips whenever the selected filter tag list changes.
        viewModel.getSelectedUserTags().addListener(
                (ListChangeListener<String>) c ->
                        rebuildChips(userTagsChipPane, viewModel.getSelectedUserTags(), viewModel::removeUserTag));

        // Draw chips for any tags already loaded from the user's saved profile.
        rebuildChips(userTagsChipPane, viewModel.getSelectedUserTags(), viewModel::removeUserTag);
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    // ── Button handlers ────────────────────────────────────────────────────────

    @FXML
    public void onAddCourseButton() {
        viewModel.addCourse();
    }

    @FXML
    public void onSaveTagsButton() {
        viewModel.saveUserTags();
    }

    @FXML
    public void onEnrollButton() {
        model.Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) { viewModel.enrollInCourse(null); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Registration");
        confirm.setHeaderText("Register for this course?");
        confirm.setContentText(nullSafe(selected.getName()));
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        viewModel.enrollInCourse(selected);
    }

    @FXML
    public void onDeleteCourseButton() {
        model.Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) { viewModel.deleteCourse(null); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete this course?");
        confirm.setContentText(nullSafe(selected.getName()));
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        viewModel.deleteCourse(selected);
    }

    @FXML
    public void onEditCourseButton() {
        model.Course selected = courseListView.getSelectionModel().getSelectedItem();
        if (selected == null) { viewModel.updateCourse(null, "", "", ""); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit your course");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(440);

        TextField nameField = new TextField(nullSafe(selected.getName()));
        TextArea  infoField = new TextArea(nullSafe(selected.getInformation()));
        infoField.setWrapText(true);
        infoField.setPrefRowCount(3);

        // Tags field in the edit dialog also stays as free text.
        TextField tagsField = new TextField(nullSafe(selected.getTags()));
        tagsField.setPromptText("e.g. java, sql, python");

        VBox content = new VBox(8,
                new Label("Course name"), nameField,
                new Label("Course info"), infoField,
                new Label("Tags (comma-separated)"), tagsField);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            viewModel.updateCourse(selected, nameField.getText(), infoField.getText(), tagsField.getText());
        }
    }

    @FXML public void onRefreshButton()  { viewModel.refreshCourses(); }
    @FXML public void onLogoutButton()   { if (onLogout != null) onLogout.run(); }

    @FXML
    public void onOpenChatButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatHistoryView.fxml"));
            Parent root = loader.load();
            ChatHistoryViewModel vm = new ChatHistoryViewModel(viewModel.getModel());
            ((ChatHistoryController) loader.getController()).init(vm);
            Stage stage = new Stage();
            stage.setTitle("Chat History");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void onOpenNotificationsButton() {
        viewModel.getModel().markAllNotificationsRead();

        if (notificationStage != null && notificationStage.isShowing()) {
            notificationStage.toFront();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NotificationView.fxml"));
            Parent root = loader.load();
            NotificationViewModel vm = new NotificationViewModel(viewModel.getModel());
            ((NotificationViewController) loader.getController()).init(vm);
            notificationStage = new Stage();
            notificationStage.setTitle("Notifications");
            notificationStage.setScene(new Scene(root));
            notificationStage.setOnHidden(e -> { vm.dispose(); notificationStage = null; });
            notificationStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void onOpenRegistrationsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistrationView.fxml"));
            Parent root = loader.load();
            RegistrationViewModel vm = new RegistrationViewModel(viewModel.getModel());
            ((RegistrationViewController) loader.getController()).init(vm);
            Stage stage = new Stage();
            stage.setTitle("My Registrations");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Chip builder ───────────────────────────────────────────────────────────

    /**
     * Clears the FlowPane and draws one pill per tag in the list.
     * Each pill has a label and an × button that calls onRemove.
     */
    private void rebuildChips(FlowPane pane, ObservableList<String> tags, Consumer<String> onRemove) {
        pane.getChildren().clear();
        for (String tag : new ArrayList<>(tags)) {
            HBox chip = new HBox(4);
            chip.setStyle("-fx-background-color: #ddf4ff; -fx-background-radius: 12; " +
                    "-fx-padding: 3 10 3 10; -fx-alignment: center-left;");
            Label lbl = new Label(tag);
            lbl.setStyle("-fx-text-fill: #0969da; -fx-font-size: 12;");
            Button x = new Button("×");
            x.setStyle("-fx-background-color: transparent; -fx-text-fill: #0969da; " +
                    "-fx-cursor: hand; -fx-padding: 0 0 0 2; -fx-font-size: 12;");
            x.setOnAction(e -> onRemove.accept(tag));
            chip.getChildren().addAll(lbl, x);
            pane.getChildren().add(chip);
        }
    }

    // ── Course list cell ───────────────────────────────────────────────────────

    private void openTutorChat(model.Course course) {
        if (course == null || course.getTutor() == null) return;
        ChatWindowManager.openChat(viewModel.getModel(), course.getTutor());
    }

    private ListCell<model.Course> createCourseCell() {
        return new ListCell<>() {
            private final VBox  content    = new VBox(4);
            private final HBox  header     = new HBox(8);
            private final Label nameLabel  = new Label();
            private final Label matchLabel = new Label("Match");
            private final Label tutorLabel = new Label();
            private final Label infoLabel  = new Label();
            private final Label tagsLabel  = new Label();

            {
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                matchLabel.setStyle("-fx-background-color: #dff0d8; -fx-text-fill: #2e7d32; " +
                        "-fx-padding: 2 8 2 8; -fx-background-radius: 4;");
                tutorLabel.setStyle("-fx-text-fill: #5d6d7e;");
                infoLabel.setStyle("-fx-text-fill: #2f2f2f;");
                tagsLabel.setStyle("-fx-text-fill: #7f8c8d;");

                nameLabel.setWrapText(true);
                infoLabel.setWrapText(true);
                tagsLabel.setWrapText(true);

                content.maxWidthProperty().bind(courseListView.widthProperty().subtract(24));
                header.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                nameLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(140));
                infoLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                tagsLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));

                header.getChildren().addAll(nameLabel, spacer, matchLabel);
                content.getChildren().addAll(header, tutorLabel, infoLabel, tagsLabel);
                content.setPadding(new Insets(6, 8, 6, 8));
            }

            @Override
            protected void updateItem(model.Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) { setText(null); setGraphic(null); return; }

                nameLabel.setText(nullSafe(course.getName()));
                tutorLabel.setText("Tutor: " + (course.getTutor() != null ? nullSafe(course.getTutor().getName()) : "Unknown"));
                infoLabel.setText(nullSafe(course.getInformation()));

                String tags = course.getTags();
                boolean hasTags = tags != null && !tags.isEmpty();
                tagsLabel.setText(hasTags ? "Tags: " + tags : "");
                tagsLabel.setVisible(hasTags);
                tagsLabel.setManaged(hasTags);

                matchLabel.setVisible(course.matchesUserTags());
                matchLabel.setManaged(course.matchesUserTags());

                setText(null);
                setGraphic(content);
            }
        };
    }

    private String nullSafe(String s) { return s != null ? s : ""; }
}
