package dao;

import model.Message;
import model.Student;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatDAOTest {

    private ChatDAO chatDAO;
    private UserDAO userDAO;
    private final String TEST_USER_1 = "ChatTester_1";
    private final String TEST_USER_2 = "ChatTester_2";

    @BeforeEach
    public void setUp() {
        chatDAO = new ChatDAO();
        userDAO = new UserDAO();

        userDAO.deleteUser(TEST_USER_1);
        userDAO.deleteUser(TEST_USER_2);

        userDAO.createUser(new Student(TEST_USER_1, "pass"));
        userDAO.createUser(new Student(TEST_USER_2, "pass"));
    }

    @AfterEach
    public void tearDown() {
        userDAO.deleteUser(TEST_USER_1);
        userDAO.deleteUser(TEST_USER_2);
    }

    @Test
    public void testSaveAndRetrieveDirectMessage() {
        User sender = new Student(TEST_USER_1, "pass");
        User receiver = new Student(TEST_USER_2, "pass");
        Message msg = new Message(sender, receiver, "Hello World from JUnit!");

        // Test save
        boolean saved = chatDAO.saveMessage(msg);
        assertTrue(saved, "Message should be saved successfully.");

        // Test retrieve
        List<Message> history = chatDAO.getDirectChatHistory(sender, receiver);
        assertFalse(history.isEmpty(), "Chat history should not be empty.");
        
        Message fetchedMsg = history.get(history.size() - 1);
        assertEquals("Hello World from JUnit!", fetchedMsg.getText());
        assertEquals(TEST_USER_1, fetchedMsg.getSender().getName());
        assertEquals(TEST_USER_2, fetchedMsg.getReceiver().getName());
    }

    @Test
    public void testGetChatPartners() {
        User user1 = new Student(TEST_USER_1, "pass");
        User user2 = new Student(TEST_USER_2, "pass");
        
        chatDAO.saveMessage(new Message(user1, user2, "Hi"));

        List<User> partners = chatDAO.getChatPartners(user1);
        assertEquals(1, partners.size(), "Should have exactly 1 partner.");
        assertEquals(TEST_USER_2, partners.get(0).getName());
    }
}
