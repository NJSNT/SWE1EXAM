package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Course;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class RegistrationViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<Course> registeredCourses;
    private StringProperty statusMessage;

    public RegistrationViewModel(ClientModel model) {
        this.model = model;
        this.registeredCourses = FXCollections.observableArrayList();
        this.statusMessage = new SimpleStringProperty("");

        this.model.addListener("RegisteredCoursesRetrieved", this);
        this.model.addListener("RegistrationCanceled", this);
        refreshRegisteredCourses();
    }

    public void refreshRegisteredCourses() {
        statusMessage.set("Loading registrations...");
        model.fetchRegisteredCourses();
    }

    public void cancelRegistration(Course course) {
        if (course == null) {
            statusMessage.set("Please select a registration to cancel");
            return;
        }

        statusMessage.set("Canceling registration...");
        model.cancelRegistration(course);
    }

    public ObservableList<Course> getRegisteredCourses() {
        return registeredCourses;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("RegisteredCoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> courses = (List<Course>) evt.getNewValue();
                registeredCourses.clear();
                registeredCourses.addAll(courses);

                if (courses.isEmpty()) {
                    statusMessage.set("No registrations yet");
                } else {
                    statusMessage.set("Registrations loaded");
                }
            } else if ("RegistrationCanceled".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Registration canceled successfully");
                    // Reload the list so the canceled course disappears.
                    refreshRegisteredCourses();
                } else {
                    statusMessage.set("Failed to cancel registration");
                }
            }
        });
    }
}
