package viewmodel;

import model.ClientModel;
import model.Course;
import model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RegistrationViewModelTest {

    private ClientModel mockModel;
    private RegistrationViewModel viewModel;

    @BeforeEach
    public void setUp() {
        mockModel = mock(ClientModel.class);
        viewModel = new RegistrationViewModel(mockModel);
    }

    // ZOMBIES - Boundary/Null Test
    @Test
    public void testCancelRegistrationWithNullCourse() {
        viewModel.cancelRegistration(null);

        assertEquals("Please select a registration to cancel", viewModel.statusMessageProperty().get());
        verify(mockModel, never()).cancelRegistration(any(Course.class));
    }

    // Scenario Testing - Sunny Day
    @Test
    public void testCancelRegistrationWithValidCourse() {
        Course course = new Course("Java", "Info", new Student("Tutor", "pass"));
        
        viewModel.cancelRegistration(course);

        assertEquals("Canceling registration...", viewModel.statusMessageProperty().get());
        verify(mockModel, times(1)).cancelRegistration(course);
    }
    
    // Scenario Testing - Refresh
    @Test
    public void testRefreshRegisteredCourses() {
        viewModel.refreshRegisteredCourses();
        
        assertEquals("Loading registrations...", viewModel.statusMessageProperty().get());
        verify(mockModel, times(2)).fetchRegisteredCourses(); // Once in constructor, once here
    }
}
