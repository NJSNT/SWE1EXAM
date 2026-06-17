package dao;

import dao.UserDAO;
import model.Student;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO;
    private final String TEST_USERNAME = "TestStudent_JUnit123";
    private final String TEST_PASSWORD = "testPassword";

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        // Ensure the test user does not exist before starting
        userDAO.deleteUser(TEST_USERNAME);
    }

    @AfterEach
    public void tearDown() {
        // Clean up the database after each test to prevent pollution
        userDAO.deleteUser(TEST_USERNAME);
    }

    // 1. Integration Test: Create and Authenticate User (Sunny Scenario)
    @Test
    public void testCreateAndAuthenticateUser() {
        User newStudent = new Student(TEST_USERNAME, TEST_PASSWORD);

        // Test Create
        boolean created = userDAO.createUser(newStudent);
        assertTrue(created, "User should be successfully created in the database.");

        // Test Authenticate (Read)
        User authenticatedUser = userDAO.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(authenticatedUser, "Authentication should return a valid User object.");
        assertEquals(TEST_USERNAME, authenticatedUser.getName(), "Username should match.");
        // Password is not tested for equality here because some systems hash it, but in
        // our code it matches
        assertEquals(TEST_PASSWORD, authenticatedUser.getPassword(), "Password should match.");
        assertTrue(authenticatedUser instanceof Student, "User should be of type Student.");
    }

    // 2. Integration Test: Authenticate Invalid User (Rainy Scenario)
    @Test
    public void testAuthenticateInvalidUser() {
        User result = userDAO.authenticateUser("NonExistentUser123", "wrongPass");
        assertNull(result, "Authenticating a non-existent user should return null.");
    }

    // 3. Integration Test: Update User Tags
    @Test
    public void testUpdateUserTags() {
        // First create the user
        User student = new Student(TEST_USERNAME, TEST_PASSWORD);
        userDAO.createUser(student);

        // Update tags
        student.setTags("java, testing, junit");
        boolean updated = userDAO.updateUserTags(student);
        assertTrue(updated, "Updating tags should return true.");

        // Verify tags are updated by authenticating again
        User fetchedUser = userDAO.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
        assertEquals("java, testing, junit", fetchedUser.getTags(), "Tags should be updated in the database.");
    }

    // 4. Integration Test: Get All Users
    @Test
    public void testGetAllUsers() {
        userDAO.createUser(new Student(TEST_USERNAME, TEST_PASSWORD));

        List<User> users = userDAO.getAllUsers();
        assertNotNull(users, "User list should not be null.");
        assertFalse(users.isEmpty(), "User list should contain at least the test user.");

        boolean foundTestUser = users.stream().anyMatch(u -> u.getName().equals(TEST_USERNAME));
        assertTrue(foundTestUser, "The newly created test user should be in the list of all users.");
    }
}
