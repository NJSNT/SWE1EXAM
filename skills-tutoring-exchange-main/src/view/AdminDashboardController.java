package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import model.Administrator;
import model.Course;
import model.User;
import viewmodel.AdminDashboardViewModel;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<User> userListView;
    @FXML private ListView<Course> courseListView;
    @FXML private Label statusLabel;
    @FXML private Button deleteUserButton;
    @FXML private Button promoteUserButton;
    @FXML private Button demoteUserButton;
    @FXML private Button deleteCourseButton;

    private AdminDashboardViewModel viewModel;
    private Runnable onLogout;

    public void init(AdminDashboardViewModel viewModel) {
        this.viewModel = viewModel;

        welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        promoteUserButton.visibleProperty().bind(viewModel.canManageUserRolesProperty());
        promoteUserButton.managedProperty().bind(viewModel.canManageUserRolesProperty());
        demoteUserButton.visibleProperty().bind(viewModel.canManageUserRolesProperty());
        demoteUserButton.managedProperty().bind(viewModel.canManageUserRolesProperty());
        deleteUserButton.disableProperty().bind(viewModel.canDeleteSelectedUserProperty().not());
        promoteUserButton.disableProperty().bind(viewModel.canPromoteSelectedUserProperty().not());
        demoteUserButton.disableProperty().bind(viewModel.canDemoteSelectedUserProperty().not());
        deleteCourseButton.disableProperty().bind(viewModel.canDeleteSelectedCourseProperty().not());

        userListView.setItems(viewModel.getUsers());
        courseListView.setItems(viewModel.getCourses());
        userListView.setCellFactory(listView -> createUserCell());
        courseListView.setCellFactory(listView -> createCourseCell());
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldUser, newUser) -> {
            viewModel.setSelectedUser(newUser);
        });
        courseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldCourse, newCourse) -> {
            viewModel.setSelectedCourse(newCourse);
        });
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshData();
    }

    @FXML
    public void onLogoutButton() {
        if (onLogout != null) {
            onLogout.run();
        }
    }

    @FXML
    public void onDeleteUserButton() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            viewModel.deleteUser(null);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete this user?");
        confirmation.setContentText(selectedUser.getName());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.deleteUser(selectedUser);
    }

    @FXML
    public void onPromoteUserButton() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            viewModel.promoteUser(null);
            return;
        }
        if (selectedUser instanceof Administrator) {
            viewModel.promoteUser(selectedUser);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Promotion");
        confirmation.setHeaderText("Promote this user to administrator?");
        confirmation.setContentText(selectedUser.getName());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.promoteUser(selectedUser);
    }

    @FXML
    public void onDemoteUserButton() {
        User selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            viewModel.demoteUser(null);
            return;
        }
        if (!(selectedUser instanceof Administrator)) {
            viewModel.demoteUser(selectedUser);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Role Change");
        confirmation.setHeaderText("Demote this administrator to student?");
        confirmation.setContentText(selectedUser.getName());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.demoteUser(selectedUser);
    }

    @FXML
    public void onDeleteCourseButton() {
        Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.deleteCourse(null);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete this course?");
        confirmation.setContentText(getCourseName(selectedCourse));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.deleteCourse(selectedCourse);
    }

    private ListCell<User> createUserCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    return;
                }

                String userType = user instanceof Administrator ? "Administrator" : "Student";
                setText(user.getName() + " (" + userType + ")");
            }
        };
    }

    private ListCell<Course> createCourseCell() {
        return new ListCell<>() {
            private final VBox content = new VBox(4);
            private final Label nameLabel = new Label();
            private final Label tutorLabel = new Label();
            private final Label infoLabel = new Label();
            private final Label tagsLabel = new Label();

            {
                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                tutorLabel.setStyle("-fx-text-fill: #5d6d7e;");
                infoLabel.setStyle("-fx-text-fill: #2f2f2f;");
                tagsLabel.setStyle("-fx-text-fill: #7f8c8d;");

                nameLabel.setWrapText(true);
                infoLabel.setWrapText(true);
                tagsLabel.setWrapText(true);

                // Keep long text inside the admin course list.
                content.maxWidthProperty().bind(courseListView.widthProperty().subtract(24));
                nameLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                infoLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                tagsLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));

                content.getChildren().addAll(nameLabel, tutorLabel, infoLabel, tagsLabel);
                content.setPadding(new Insets(6, 8, 6, 8));
            }

            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);

                if (empty || course == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                nameLabel.setText(getCourseName(course));
                tutorLabel.setText("Tutor: " + getTutorName(course));
                infoLabel.setText(safeText(course.getInformation()));

                String tags = course.getTags();
                if (tags == null || tags.isEmpty()) {
                    tagsLabel.setText("");
                    tagsLabel.setVisible(false);
                    tagsLabel.setManaged(false);
                } else {
                    tagsLabel.setText("Tags: " + tags);
                    tagsLabel.setVisible(true);
                    tagsLabel.setManaged(true);
                }

                setText(null);
                setGraphic(content);
            }
        };
    }

    private String getCourseName(Course course) {
        if (course == null || course.getName() == null) {
            return "";
        }
        return course.getName();
    }

    private String getTutorName(Course course) {
        if (course.getTutor() == null || course.getTutor().getName() == null) {
            return "Unknown";
        }
        return course.getTutor().getName();
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }
}
