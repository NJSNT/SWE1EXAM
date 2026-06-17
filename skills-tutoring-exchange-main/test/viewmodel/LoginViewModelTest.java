package viewmodel;

import javafx.scene.paint.Paint;
import model.ClientModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import viewmodel.LoginViewModel;

import java.beans.PropertyChangeEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginViewModelTest {

    private ClientModel mockModel;
    private LoginViewModel viewModel;

    @BeforeEach
    public void setUp() {
        // Create a Mock for the ClientModel using Mockito
        mockModel = mock(ClientModel.class);
        viewModel = new LoginViewModel(mockModel);
    }

    // 1. Alternate/Rainy Scenarios: Empty Fields Validation
    @Test
    public void testLoginWithEmptyUsername() {
        viewModel.usernameProperty().set("");
        viewModel.passwordProperty().set("password");

        viewModel.login();

        assertEquals("Please enter both username and password", viewModel.errorProperty().get(), "Should display error for empty username.");
        assertEquals(Paint.valueOf("#e74c3c"), viewModel.statusColorProperty().get(), "Status color should be red.");
        
        // Verify model.login() was NEVER called because of validation failure
        verify(mockModel, never()).login(anyString(), anyString());
    }

    @Test
    public void testLoginWithEmptyPassword() {
        viewModel.usernameProperty().set("user");
        viewModel.passwordProperty().set("");

        viewModel.login();

        assertEquals("Please enter both username and password", viewModel.errorProperty().get(), "Should display error for empty password.");
        verify(mockModel, never()).login(anyString(), anyString());
    }

    // 2. Main/Sunny Scenario: Valid Fields
    @Test
    public void testLoginWithValidCredentials() {
        viewModel.usernameProperty().set("user");
        viewModel.passwordProperty().set("pass");

        viewModel.login();

        assertEquals("Connecting...", viewModel.errorProperty().get(), "Should display connecting message.");
        assertEquals(Paint.valueOf("#34495e"), viewModel.statusColorProperty().get(), "Status color should be info blue.");
        
        // Verify model.login() was called exactly once with the correct parameters
        verify(mockModel, times(1)).login("user", "pass");
    }

    // 3. Registering Student Validation
    @Test
    public void testRegisterStudentEmptyFields() {
        viewModel.usernameProperty().set("");
        viewModel.passwordProperty().set("pass");

        viewModel.registerStudent();

        assertEquals("Please enter both username and password to register", viewModel.errorProperty().get());
        verify(mockModel, never()).registerStudent(anyString(), anyString());
    }

    @Test
    public void testRegisterStudentValidFields() {
        viewModel.usernameProperty().set("newuser");
        viewModel.passwordProperty().set("newpass");

        viewModel.registerStudent();

        assertEquals("Registering...", viewModel.errorProperty().get());
        verify(mockModel, times(1)).registerStudent("newuser", "newpass");
    }

    // 4. Reset Form behavior
    @Test
    public void testResetForm() {
        viewModel.usernameProperty().set("user");
        viewModel.passwordProperty().set("pass");
        viewModel.errorProperty().set("Some error");
        
        viewModel.resetForm();
        
        assertEquals("", viewModel.usernameProperty().get());
        assertEquals("", viewModel.passwordProperty().get());
        assertEquals("", viewModel.errorProperty().get());
        assertEquals(Paint.valueOf("#e74c3c"), viewModel.statusColorProperty().get());
    }
}
