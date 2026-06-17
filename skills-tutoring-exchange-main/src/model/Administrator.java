package model;

import java.util.List;

public class Administrator extends User {
    
    public Administrator(String name, String password) {
        super(name, password);
    }

    // Administrator specific methods to manage courses and users
    // Note: In a real system, these would likely interact with a Database or a central SystemManager
    
    public void deleteCourse(List<Course> systemCourses, Course course) {
        if (systemCourses.contains(course)) {
            // Remove the course from the tutor's provided list
            Student tutor = course.getTutor();
            if (tutor != null) {
                tutor.removeProvidedCourse(course);
            }
            // Cancel enrollments for all students in this course
            for (Student student : course.getEnrolledStudents()) {
                student.getEnrolledCourses().remove(course);
            }
            systemCourses.remove(course);
        }
    }

    public void deleteAccount(List<User> systemUsers, User userToDelete) {
        systemUsers.remove(userToDelete);
    }
}
