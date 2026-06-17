package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Administrator;
import model.ClientModel;
import model.Course;
import model.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class AdminDashboardViewModel implements PropertyChangeListener {
    private static final String MAIN_ADMIN_NAME = "admin";
    private ClientModel model;
    private ObservableList<User> users;
    private ObservableList<Course> courses;
    private StringProperty welcomeMessage;
    private StringProperty statusMessage;
    private BooleanProperty canManageUserRoles;
    private BooleanProperty canDeleteSelectedUser;
    private BooleanProperty canPromoteSelectedUser;
    private BooleanProperty canDemoteSelectedUser;
    private BooleanProperty canDeleteSelectedCourse;
    private User selectedUser;
    private Course selectedCourse;

    public AdminDashboardViewModel(ClientModel model) {
        this.model = model;
        this.users = FXCollections.observableArrayList();
        this.courses = FXCollections.observableArrayList();
        this.welcomeMessage = new SimpleStringProperty("Admin Dashboard - " + model.getCurrentUser().getName());
        this.statusMessage = new SimpleStringProperty("");
        this.canManageUserRoles = new SimpleBooleanProperty(currentUserIsMainAdmin());
        this.canDeleteSelectedUser = new SimpleBooleanProperty(false);
        this.canPromoteSelectedUser = new SimpleBooleanProperty(false);
        this.canDemoteSelectedUser = new SimpleBooleanProperty(false);
        this.canDeleteSelectedCourse = new SimpleBooleanProperty(false);

        this.model.addListener("UsersRetrieved", this);
        this.model.addListener("CoursesRetrieved", this);
        this.model.addListener("AdminUserDeleted", this);
        this.model.addListener("AdminCourseDeleted", this);
        this.model.addListener("AdminUserPromoted", this);
        this.model.addListener("AdminUserDemoted", this);

        refreshData();
    }

    public void refreshData() {
        statusMessage.set("Loading admin data...");
        model.fetchUsers();
        model.fetchCourses();
    }

    public void deleteUser(User user) {
        if (user == null) {
            statusMessage.set("Please select a user to delete");
            return;
        }
        if (user.getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You cannot delete your own admin account");
            return;
        }
        if (MAIN_ADMIN_NAME.equals(user.getName())) {
            statusMessage.set("The main admin account cannot be deleted");
            return;
        }
        if (user instanceof Administrator && !currentUserIsMainAdmin()) {
            statusMessage.set("Only the main admin can delete administrators");
            return;
        }

        statusMessage.set("Deleting user...");
        model.adminDeleteUser(user);
    }

    public void promoteUser(User user) {
        if (user == null) {
            statusMessage.set("Please select a user to promote");
            return;
        }
        if (!currentUserIsMainAdmin()) {
            statusMessage.set("Only the main admin can change user roles");
            return;
        }
        if (user instanceof Administrator) {
            statusMessage.set("This user is already an administrator");
            return;
        }

        statusMessage.set("Promoting user...");
        model.adminPromoteUser(user);
    }

    public void demoteUser(User user) {
        if (user == null) {
            statusMessage.set("Please select a user to demote");
            return;
        }
        if (!currentUserIsMainAdmin()) {
            statusMessage.set("Only the main admin can change user roles");
            return;
        }
        if (MAIN_ADMIN_NAME.equals(user.getName())) {
            statusMessage.set("The main admin account cannot be changed");
            return;
        }
        if (!(user instanceof Administrator)) {
            statusMessage.set("This user is already a student");
            return;
        }

        statusMessage.set("Demoting user...");
        model.adminDemoteUser(user);
    }

    public void deleteCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to delete");
            return;
        }

        statusMessage.set("Deleting course...");
        model.adminDeleteCourse(course);
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public ObservableList<Course> getCourses() {
        return courses;
    }

    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public BooleanProperty canManageUserRolesProperty() {
        return canManageUserRoles;
    }

    public BooleanProperty canDeleteSelectedUserProperty() {
        return canDeleteSelectedUser;
    }

    public BooleanProperty canPromoteSelectedUserProperty() {
        return canPromoteSelectedUser;
    }

    public BooleanProperty canDemoteSelectedUserProperty() {
        return canDemoteSelectedUser;
    }

    public BooleanProperty canDeleteSelectedCourseProperty() {
        return canDeleteSelectedCourse;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
        updateUserButtonState();
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
        updateCourseButtonState();
    }

    public void dispose() {
        model.removeListener("UsersRetrieved", this);
        model.removeListener("CoursesRetrieved", this);
        model.removeListener("AdminUserDeleted", this);
        model.removeListener("AdminCourseDeleted", this);
        model.removeListener("AdminUserPromoted", this);
        model.removeListener("AdminUserDemoted", this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("UsersRetrieved".equals(evt.getPropertyName())) {
                List<User> receivedUsers = (List<User>) evt.getNewValue();
                users.clear();
                users.addAll(receivedUsers);
                statusMessage.set("Admin data loaded");
            } else if ("CoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> receivedCourses = (List<Course>) evt.getNewValue();
                courses.clear();
                courses.addAll(receivedCourses);
                statusMessage.set("Admin data loaded");
            } else if ("AdminUserDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("User deleted successfully");
                    refreshData();
                } else {
                    statusMessage.set("Failed to delete user");
                }
            } else if ("AdminCourseDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course deleted successfully");
                    refreshData();
                } else {
                    statusMessage.set("Failed to delete course");
                }
            } else if ("AdminUserPromoted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("User promoted to administrator");
                    refreshData();
                } else {
                    statusMessage.set("Failed to promote user");
                }
            } else if ("AdminUserDemoted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("User demoted to student");
                    refreshData();
                } else {
                    statusMessage.set("Failed to demote user");
                }
            }
        });
    }

    private boolean currentUserIsMainAdmin() {
        return model.getCurrentUser() instanceof Administrator
                && MAIN_ADMIN_NAME.equals(model.getCurrentUser().getName());
    }

    private void updateUserButtonState() {
        boolean hasUser = selectedUser != null;
        boolean selectedIsMainAdmin = hasUser && MAIN_ADMIN_NAME.equals(selectedUser.getName());
        boolean selectedIsCurrentUser = hasUser && selectedUser.getName().equals(model.getCurrentUser().getName());
        boolean selectedIsAdmin = selectedUser instanceof Administrator;
        boolean currentIsMainAdmin = currentUserIsMainAdmin();

        canDeleteSelectedUser.set(hasUser
                && !selectedIsCurrentUser
                && !selectedIsMainAdmin
                && (!selectedIsAdmin || currentIsMainAdmin));

        canPromoteSelectedUser.set(hasUser
                && currentIsMainAdmin
                && !selectedIsAdmin);

        canDemoteSelectedUser.set(hasUser
                && currentIsMainAdmin
                && selectedIsAdmin
                && !selectedIsMainAdmin
                && !selectedIsCurrentUser);
    }

    private void updateCourseButtonState() {
        canDeleteSelectedCourse.set(selectedCourse != null);
    }
}
