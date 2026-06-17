package viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Course;
import model.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DashboardViewModel implements PropertyChangeListener {
    private ClientModel model;

    // Properties for UI binding
    private StringProperty welcomeMessage;
    private StringProperty searchText;
    private StringProperty newCourseName;
    private StringProperty newCourseInfo;
    // Bound to the free-text Tags field in "Offer a New Course" — unchanged from original.
    private StringProperty newCourseTags;
    private StringProperty statusMessage;
    private StringProperty notificationsButtonText;
    private BooleanProperty canEditSelectedCourse;
    private BooleanProperty canDeleteSelectedCourse;
    private BooleanProperty canEnrollSelectedCourse;
    private ObservableList<Course> allCourses;
    private ObservableList<Course> courseList;
    private Set<Integer> registeredCourseIds;
    private Course selectedCourse;
    private Course courseForEnrollment;
    private Course lastEnrolledCourse;
    private Runnable onEnrollmentSuccess;

    // All tags known to the system — fetched from server, populates the filter dropdown.
    private ObservableList<String> availableTags;
    // Tags the user has selected in the filter dropdown — drives course matching/ranking.
    private ObservableList<String> selectedUserTags;

    public DashboardViewModel(ClientModel model) {
        this.model = model;
        this.welcomeMessage = new SimpleStringProperty("Welcome, " + model.getCurrentUser().getName());
        this.searchText = new SimpleStringProperty("");
        this.newCourseName = new SimpleStringProperty("");
        this.newCourseInfo = new SimpleStringProperty("");
        this.newCourseTags = new SimpleStringProperty("");
        this.statusMessage = new SimpleStringProperty("");
        this.notificationsButtonText = new SimpleStringProperty("Notifications");
        this.canEditSelectedCourse = new SimpleBooleanProperty(false);
        this.canDeleteSelectedCourse = new SimpleBooleanProperty(false);
        this.canEnrollSelectedCourse = new SimpleBooleanProperty(false);
        this.allCourses = FXCollections.observableArrayList();
        this.courseList = FXCollections.observableArrayList();
        this.registeredCourseIds = new HashSet<>();

        this.availableTags = FXCollections.observableArrayList();

        // Seed selectedUserTags from the user's saved profile so chips appear immediately.
        this.selectedUserTags = FXCollections.observableArrayList();
        String savedTags = model.getCurrentUser().getTags();
        if (savedTags != null && !savedTags.isEmpty()) {
            for (String tag : savedTags.split(",")) {
                String clean = tag.trim();
                if (!clean.isEmpty()) selectedUserTags.add(clean);
            }
        }

        this.searchText.addListener((observable, oldValue, newValue) -> applyCourseFilter());

        this.model.addListener("CoursesRetrieved", this);
        this.model.addListener("CourseAdded", this);
        this.model.addListener("CourseDeleted", this);
        this.model.addListener("CourseUpdated", this);
        this.model.addListener("CourseEnrolled", this);
        this.model.addListener("RegisteredCoursesRetrieved", this);
        this.model.addListener("TagsRetrieved", this);
        this.model.addListener("UserTagsUpdated", this);
        this.model.addListener("NewNotification", this);
        this.model.addListener("NotificationsRead", this);
        this.model.addListener("NotificationRemoved", this);
        this.model.addListener("NotificationsCleared", this);

        this.model.fetchCourses();
        this.model.fetchRegisteredCourses();
        this.model.fetchTags();
    }

    // ── Filter tag selection ───────────────────────────────────────────────────

    public void addUserTag(String tag) {
        if (tag != null && !tag.isEmpty() && !selectedUserTags.contains(tag)) {
            selectedUserTags.add(tag);
            applyCourseFilter();
        }
    }

    public void removeUserTag(String tag) {
        selectedUserTags.remove(tag);
        applyCourseFilter();
    }

    // ── Course operations ─────────────────────────────────────────────────────

    public void addCourse() {
        if (newCourseName.get().isEmpty() || newCourseInfo.get().isEmpty()) {
            statusMessage.set("Please fill in both course name and info");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can add courses as Tutors");
            return;
        }
        Student tutor = (Student) model.getCurrentUser();
        // newCourseTags is the raw comma-separated text the user typed —
        // the server normalizes and upserts each tag into the tags table.
        Course newCourse = new Course(newCourseName.get(), newCourseInfo.get(), newCourseTags.get(), tutor);
        statusMessage.set("Adding course...");
        model.addCourse(newCourse);
    }

    public void deleteCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to delete");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only delete courses you created");
            return;
        }
        statusMessage.set("Deleting course...");
        model.deleteCourse(course);
    }

    public void updateCourse(Course course, String newName, String newInformation, String newTags) {
        if (course == null) {
            statusMessage.set("Please select a course to edit");
            return;
        }
        if (newName.isEmpty() || newInformation.isEmpty()) {
            statusMessage.set("Please fill in both course name and info");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only edit courses you created");
            return;
        }
        Course updatedCourse = new Course(newName, newInformation, newTags, course.getTutor());
        updatedCourse.setId(course.getId());
        statusMessage.set("Updating course...");
        model.updateCourse(updatedCourse);
    }

    public void saveUserTags() {
        statusMessage.set("Saving your tags...");
        String tagsString = String.join(", ", selectedUserTags);
        model.updateCurrentUserTags(tagsString);
    }

    public void refreshCourses() {
        statusMessage.set("Refreshing courses...");
        model.fetchCourses();
    }

    public void enrollInCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to enroll in");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can enroll in courses");
            return;
        }
        statusMessage.set("Enrolling in " + course.getName() + "...");
        courseForEnrollment = course;
        model.enrollCourse(course);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public StringProperty welcomeMessageProperty()        { return welcomeMessage; }
    public StringProperty searchTextProperty()            { return searchText; }
    public StringProperty newCourseNameProperty()         { return newCourseName; }
    public StringProperty newCourseInfoProperty()         { return newCourseInfo; }
    public StringProperty newCourseTagsProperty()         { return newCourseTags; }
    public StringProperty statusMessageProperty()         { return statusMessage; }
    public StringProperty notificationsButtonTextProperty(){ return notificationsButtonText; }
    public BooleanProperty canEditSelectedCourseProperty()  { return canEditSelectedCourse; }
    public BooleanProperty canDeleteSelectedCourseProperty(){ return canDeleteSelectedCourse; }
    public BooleanProperty canEnrollSelectedCourseProperty(){ return canEnrollSelectedCourse; }
    public ObservableList<Course>  getCourseList()        { return courseList; }
    public ObservableList<String>  getAvailableTags()     { return availableTags; }
    public ObservableList<String>  getSelectedUserTags()  { return selectedUserTags; }
    public ClientModel             getModel()             { return model; }
    public Course                  getLastEnrolledCourse(){ return lastEnrolledCourse; }

    public void setSelectedCourse(Course course) {
        this.selectedCourse = course;
        updateButtonState();
    }

    public void setOnEnrollmentSuccess(Runnable r) {
        this.onEnrollmentSuccess = r;
    }

    public void dispose() {
        model.removeListener("CoursesRetrieved", this);
        model.removeListener("CourseAdded", this);
        model.removeListener("CourseDeleted", this);
        model.removeListener("CourseUpdated", this);
        model.removeListener("CourseEnrolled", this);
        model.removeListener("RegisteredCoursesRetrieved", this);
        model.removeListener("TagsRetrieved", this);
        model.removeListener("UserTagsUpdated", this);
        model.removeListener("NewNotification", this);
        model.removeListener("NotificationsRead", this);
        model.removeListener("NotificationRemoved", this);
        model.removeListener("NotificationsCleared", this);
    }

    // ── Filtering / matching ──────────────────────────────────────────────────

    private void applyCourseFilter() {
        List<Course> filtered = new ArrayList<>();
        for (Course course : allCourses) {
            if (courseMatchesSearch(course)) filtered.add(course);
        }
        showCoursesWithMatchesFirst(filtered);

        if (allCourses.isEmpty()) {
            statusMessage.set("No courses available");
        } else if (filtered.isEmpty()) {
            statusMessage.set("No courses found");
        } else {
            statusMessage.set("Courses updated");
        }
    }

    private boolean courseMatchesSearch(Course course) {
        String search = searchText.get();
        if (search == null || search.trim().isEmpty()) return true;
        String lower = search.toLowerCase().trim();
        String text = (nullSafe(course.getName()) + " " +
                nullSafe(course.getInformation()) + " " +
                nullSafe(course.getTags()) + " " +
                (course.getTutor() != null ? nullSafe(course.getTutor().getName()) : "")).toLowerCase();
        return text.contains(lower);
    }

    private void showCoursesWithMatchesFirst(List<Course> courses) {
        List<Course> matching = new ArrayList<>();
        List<Course> other    = new ArrayList<>();
        for (Course course : courses) {
            boolean match = courseMatchesUserTags(course);
            course.setMatchesUserTags(match);
            if (match) matching.add(course); else other.add(course);
        }
        courseList.clear();
        courseList.addAll(matching);
        courseList.addAll(other);
    }

    /**
     * Returns true if at least one of the user's selected filter tags appears
     * in the course's tag list. Both sides are lowercase (tags are normalized
     * server-side on save; selectedUserTags only contains tags from the dropdown
     * which are already normalized).
     */
    private boolean courseMatchesUserTags(Course course) {
        if (selectedUserTags.isEmpty()) return false;
        String courseTags = course.getTags();
        if (courseTags == null || courseTags.isEmpty()) return false;

        List<String> courseTagList = Arrays.stream(courseTags.split(","))
                .map(String::trim).collect(Collectors.toList());

        for (String userTag : selectedUserTags) {
            if (courseTagList.contains(userTag.trim())) return true;
        }
        return false;
    }

    // ── PropertyChangeListener ────────────────────────────────────────────────

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            switch (evt.getPropertyName()) {
                case "CoursesRetrieved":
                    List<Course> courses = (List<Course>) evt.getNewValue();
                    allCourses.clear();
                    allCourses.addAll(courses);
                    applyCourseFilter();
                    break;
                case "TagsRetrieved":
                    List<String> tags = (List<String>) evt.getNewValue();
                    availableTags.clear();
                    availableTags.addAll(tags);
                    // Ensure any pre-existing user tags (from profile) are in the list.
                    for (String t : selectedUserTags) {
                        if (!availableTags.contains(t)) availableTags.add(t);
                    }
                    break;
                case "CourseAdded":
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        statusMessage.set("Course added successfully!");
                        newCourseName.set("");
                        newCourseInfo.set("");
                        newCourseTags.set("");
                        model.fetchCourses();
                        // Refresh dropdown in case new tags were introduced.
                        model.fetchTags();
                    } else {
                        statusMessage.set("Failed to add course");
                    }
                    break;
                case "CourseDeleted":
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        statusMessage.set("Course deleted successfully!");
                        model.fetchCourses();
                    } else {
                        statusMessage.set("Failed to delete course");
                    }
                    break;
                case "CourseUpdated":
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        statusMessage.set("Course updated successfully!");
                        model.fetchCourses();
                        model.fetchTags();
                    } else {
                        statusMessage.set("Failed to update course");
                    }
                    break;
                case "CourseEnrolled":
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        statusMessage.set("Successfully enrolled in the course!");
                        lastEnrolledCourse = courseForEnrollment;
                        if (lastEnrolledCourse != null) registeredCourseIds.add(lastEnrolledCourse.getId());
                        courseForEnrollment = null;
                        updateButtonState();
                        if (onEnrollmentSuccess != null) onEnrollmentSuccess.run();
                    } else {
                        statusMessage.set("Failed to enroll. Maybe already enrolled?");
                        courseForEnrollment = null;
                        updateButtonState();
                    }
                    break;
                case "RegisteredCoursesRetrieved":
                    List<Course> registered = (List<Course>) evt.getNewValue();
                    registeredCourseIds.clear();
                    for (Course c : registered) registeredCourseIds.add(c.getId());
                    updateButtonState();
                    break;
                case "UserTagsUpdated":
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        statusMessage.set("Your tags were saved");
                        model.fetchCourses();
                    } else {
                        statusMessage.set("Failed to save your tags");
                    }
                    break;
                case "NewNotification":
                    model.Notification notif = (model.Notification) evt.getNewValue();
                    statusMessage.set("Notification — " + notif.getTitle() + ": " + notif.getMessageInformation());
                    updateNotificationsButtonText();
                    break;
                case "NotificationsRead":
                case "NotificationRemoved":
                    updateNotificationsButtonText();
                    break;
                case "NotificationsCleared":
                    statusMessage.set("Notifications cleared");
                    updateNotificationsButtonText();
                    break;
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateNotificationsButtonText() {
        int count = model.getUnreadNotificationCount();
        notificationsButtonText.set(count == 0 ? "Notifications" : "Notifications (" + count + ")");
    }

    private void updateButtonState() {
        boolean owns = currentUserOwnsCourse(selectedCourse);
        boolean canEnroll = selectedCourse != null
                && !owns
                && !registeredCourseIds.contains(selectedCourse.getId())
                && model.getCurrentUser() instanceof Student;
        canEditSelectedCourse.set(owns);
        canDeleteSelectedCourse.set(owns);
        canEnrollSelectedCourse.set(canEnroll);
    }

    private boolean currentUserOwnsCourse(Course course) {
        if (course == null || course.getTutor() == null || model.getCurrentUser() == null) return false;
        return course.getTutor().getName().equals(model.getCurrentUser().getName());
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}
