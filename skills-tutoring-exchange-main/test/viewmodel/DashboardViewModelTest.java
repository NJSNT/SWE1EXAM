package viewmodel;

import model.ClientModel;
import model.Course;
import model.Student;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DashboardViewModelTest {

    private ClientModel mockModel;
    private DashboardViewModel viewModel;
    private User testStudent;

    @BeforeEach
    public void setUp() {
        mockModel = mock(ClientModel.class);
        testStudent = new Student("TestTutor", "pass");
        
        // DashboardViewModel constructor calls model.getCurrentUser()
        when(mockModel.getCurrentUser()).thenReturn(testStudent);
        
        viewModel = new DashboardViewModel(mockModel);
    }

    // Sunny Scenario: Valid course addition
    @Test
    public void testAddCourseValid() {
        viewModel.newCourseNameProperty().set("Java 101");
        viewModel.newCourseInfoProperty().set("Intro to Java");
        
        viewModel.addCourse();
        
        assertEquals("Adding course...", viewModel.statusMessageProperty().get());
        verify(mockModel, times(1)).addCourse(any(Course.class));
    }

    // Rainy Scenario: Missing fields
    @Test
    public void testAddCourseMissingFields() {
        viewModel.newCourseNameProperty().set("");
        viewModel.newCourseInfoProperty().set("Intro to Java");
        
        viewModel.addCourse();
        
        assertEquals("Please fill in both course name and info", viewModel.statusMessageProperty().get());
        verify(mockModel, never()).addCourse(any(Course.class));
    }

    // Boundary Test: Delete null course
    @Test
    public void testDeleteNullCourse() {
        viewModel.deleteCourse(null);
        
        assertEquals("Please select a course to delete", viewModel.statusMessageProperty().get());
        verify(mockModel, never()).deleteCourse(any(Course.class));
    }

    // Scenario Testing: Enroll in course
    @Test
    public void testEnrollInCourse() {
        Student otherTutor = new Student("OtherTutor", "pass");
        Course courseToEnroll = new Course("Python", "Learn Python", otherTutor);
        
        viewModel.enrollInCourse(courseToEnroll);
        
        assertEquals("Enrolling in Python...", viewModel.statusMessageProperty().get());
        verify(mockModel, times(1)).enrollCourse(courseToEnroll);
    }
}
