package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import model.Course;
import viewmodel.RegistrationViewModel;

public class RegistrationViewController {

    @FXML private ListView<Course> registeredCourseListView;
    @FXML private Label statusLabel;

    private RegistrationViewModel viewModel;

    public void init(RegistrationViewModel viewModel) {
        this.viewModel = viewModel;

        registeredCourseListView.setItems(viewModel.getRegisteredCourses());
        registeredCourseListView.setCellFactory(listView -> createCourseCell());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
    }

    @FXML
    public void onCancelRegistrationButton() {
        Course selectedCourse = registeredCourseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.cancelRegistration(null);
            return;
        }

        // The student should confirm before the registration is removed.
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Cancel");
        confirmation.setHeaderText("Cancel this registration?");
        confirmation.setContentText(getCourseName(selectedCourse));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.cancelRegistration(selectedCourse);
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshRegisteredCourses();
    }

    private String getCourseName(Course course) {
        if (course == null || course.getName() == null) {
            return "";
        }
        return course.getName();
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

                // Keep long course descriptions readable inside the list.
                content.maxWidthProperty().bind(registeredCourseListView.widthProperty().subtract(24));
                nameLabel.maxWidthProperty().bind(registeredCourseListView.widthProperty().subtract(40));
                infoLabel.maxWidthProperty().bind(registeredCourseListView.widthProperty().subtract(40));
                tagsLabel.maxWidthProperty().bind(registeredCourseListView.widthProperty().subtract(40));

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
