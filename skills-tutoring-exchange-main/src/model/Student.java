package model;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private List<Course> providedCourses;
    private List<Course> enrolledCourses;

    public Student(String name, String password) {
        super(name, password);
        this.providedCourses = new ArrayList<>();
        this.enrolledCourses = new ArrayList<>();
    }

    public List<Course> getProvidedCourses() {
        return providedCourses;
    }

    public void addProvidedCourse(Course course) {
        if (!this.providedCourses.contains(course)) {
            this.providedCourses.add(course);
            course.setTutor(this);
        }
    }

    public void removeProvidedCourse(Course course) {
        this.providedCourses.remove(course);
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void enrollInCourse(Course course) {
        if (!this.enrolledCourses.contains(course)) {
            this.enrolledCourses.add(course);
            course.addEnrolledStudent(this);
        }
    }

    public void cancelEnrollment(Course course) {
        this.enrolledCourses.remove(course);
        course.removeEnrolledStudent(this);
    }
}
