package model;

import model.Course;
import model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CourseTest {

    private Course course;
    private Student tutor;

    @BeforeEach
    public void setUp() {
        tutor = new Student("TutorBob", "pass");
        course = new Course("Java Basics", "Learn Java from scratch", tutor);
    }

    // 1. Boundaries & Nulls: Testing tags
    @Test
    public void testSetTagsWithNull() {
        course.setTags(null);
        assertEquals("", course.getTags(), "Course tags should be empty string when set to null.");
    }

    @Test
    public void testSetTagsWithWhitespaces() {
        course.setTags("  python, machine learning  ");
        assertEquals("python, machine learning", course.getTags(), "Course tags should be trimmed.");
    }

    // 2. Zero/One/Many: Testing enrolled students
    @Test
    public void testZeroEnrolledStudents() {
        assertTrue(course.getEnrolledStudents().isEmpty(), "Course should have 0 enrolled students initially.");
    }

    @Test
    public void testAddOneEnrolledStudent() {
        Student student = new Student("Alice", "pass");
        course.addEnrolledStudent(student);

        assertEquals(1, course.getEnrolledStudents().size(), "Should have exactly 1 enrolled student.");
        assertTrue(course.getEnrolledStudents().contains(student), "The student should be in the enrolled list.");
    }

    @Test
    public void testAddManyEnrolledStudents() {
        Student student1 = new Student("Alice", "pass");
        Student student2 = new Student("Charlie", "pass");
        
        course.addEnrolledStudent(student1);
        course.addEnrolledStudent(student2);

        assertEquals(2, course.getEnrolledStudents().size(), "Should have 2 enrolled students.");
    }

    @Test
    public void testAddSameStudentMultipleTimes() {
        Student student = new Student("Alice", "pass");
        course.addEnrolledStudent(student);
        course.addEnrolledStudent(student); // Duplicate addition attempt

        assertEquals(1, course.getEnrolledStudents().size(), "Duplicate students should not be added.");
    }

    @Test
    public void testRemoveEnrolledStudent() {
        Student student = new Student("Alice", "pass");
        course.addEnrolledStudent(student);
        course.removeEnrolledStudent(student);

        assertTrue(course.getEnrolledStudents().isEmpty(), "Student list should be empty after removal.");
    }

    @Test
    public void testToStringWithMatchesUserTags() {
        course.setMatchesUserTags(true);
        String result = course.toString();
        assertTrue(result.startsWith("[Match]"), "Course string representation should indicate if it matches user tags.");
    }
}
