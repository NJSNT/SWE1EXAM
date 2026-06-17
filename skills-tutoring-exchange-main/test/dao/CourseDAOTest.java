package dao;

import model.Course;
import model.Student;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CourseDAOTest {

    private CourseDAO courseDAO;
    private UserDAO userDAO;
    private final String TUTOR_NAME = "TestTutor_JUnit";
    private final String STUDENT_NAME = "TestStudent_JUnit";
    private Student tutor;
    private Student student;

    @BeforeEach
    public void setUp() {
        courseDAO = new CourseDAO();
        userDAO = new UserDAO();

        userDAO.deleteUser(TUTOR_NAME);
        userDAO.deleteUser(STUDENT_NAME);

        tutor = new Student(TUTOR_NAME, "pass");
        student = new Student(STUDENT_NAME, "pass");
        userDAO.createUser(tutor);
        userDAO.createUser(student);
    }

    @AfterEach
    public void tearDown() {
        userDAO.deleteUser(TUTOR_NAME);
        userDAO.deleteUser(STUDENT_NAME);
    }

    @Test
    public void testAddAndGetAllCourses() {
        Course newCourse = new Course("JUnit Mastery", "Learn how to test", "java, testing", tutor);
        
        // Test Add
        boolean added = courseDAO.addCourse(newCourse);
        assertTrue(added, "Course should be added to the database.");

        // Test Get All
        List<Course> courses = courseDAO.getAllCourses();
        boolean found = false;
        for (Course c : courses) {
            if (c.getName().equals("JUnit Mastery") && c.getTutor().getName().equals(TUTOR_NAME)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "The newly added course should be in the global list.");
    }

    @Test
    public void testEnrollStudent() {
        Course newCourse = new Course("Advanced SQL", "Queries", tutor);
        courseDAO.addCourse(newCourse);
        
        // Need to fetch course to get its auto-generated ID, but enrollStudent handles finding it by name/tutor if ID is -1
        boolean enrolled = courseDAO.enrollStudent(student, newCourse);
        assertTrue(enrolled, "Student should be enrolled successfully.");

        // Verify registration
        List<Course> registered = courseDAO.getRegisteredCourses(student);
        boolean found = registered.stream().anyMatch(c -> c.getName().equals("Advanced SQL"));
        assertTrue(found, "Student's registered courses should include the new course.");
    }
}
