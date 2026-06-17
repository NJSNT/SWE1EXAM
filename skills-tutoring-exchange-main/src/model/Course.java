package model;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Course implements Serializable {
    private int id;
    private String name;
    private String information;
    private String tags;
    private boolean matchesUserTags;
    private Student tutor; // The student who provides the course
    private List<Student> enrolledStudents; // Students enrolled in the course

    public Course(String name, String information, Student tutor) {
        this(name, information, "", tutor);
    }

    public Course(String name, String information, String tags, Student tutor) {
        this.id = -1;
        this.name = name;
        this.information = information;
        setTags(tags);
        this.matchesUserTags = false;
        this.tutor = tutor;
        this.enrolledStudents = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        // Keep tags as a simple text value, like "java, sql, oop"
        if (tags == null) {
            this.tags = "";
        } else {
            this.tags = tags.trim();
        }
    }

    public boolean matchesUserTags() {
        return matchesUserTags;
    }

    public void setMatchesUserTags(boolean matchesUserTags) {
        this.matchesUserTags = matchesUserTags;
    }

    public Student getTutor() {
        return tutor;
    }

    public void setTutor(Student tutor) {
        this.tutor = tutor;
    }

    public List<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void addEnrolledStudent(Student student) {
        if (!this.enrolledStudents.contains(student)) {
            this.enrolledStudents.add(student);
            student.enrollInCourse(this);
        }
    }

    public void removeEnrolledStudent(Student student) {
        this.enrolledStudents.remove(student);
    }

    @Override
    public String toString() {
        String courseText = name + " (Tutor: " + tutor.getName() + ") - " + information;

        // Matching courses are marked so students can see why they are at the top
        if (matchesUserTags) {
            courseText = "[Match] " + courseText;
        }

        // Show tags only when the course has them
        if (tags != null && !tags.isEmpty()) {
            courseText += " [Tags: " + tags + "]";
        }

        return courseText;
    }
}
