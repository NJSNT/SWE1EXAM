package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    private User sender;
    private User receiver;
    private Message message;

    @BeforeEach
    public void setUp() {
        sender = new Student("Alice", "pass");
        receiver = new Student("Bob", "pass");
        message = new Message(sender, receiver, "Hello, Bob!");
    }

    // Boundary / Null testing
    @Test
    public void testMessageWithNullReceiver() {
        Message broadcastMsg = new Message(sender, "Broadcast message");
        assertNull(broadcastMsg.getReceiver(), "Receiver should be null if not provided.");
        assertEquals("Broadcast message", broadcastMsg.getText());
    }

    // Behavior testing
    @Test
    public void testMessageTimestampInitialization() {
        assertNotNull(message.getTimeStamp(), "Timestamp should be initialized upon creation.");
        assertTrue(message.getTimeStamp().isBefore(LocalDateTime.now().plusSeconds(1)), "Timestamp should be roughly now.");
    }

    // Scenario testing: toString format
    @Test
    public void testMessageToStringFormat() {
        String stringRepresentation = message.toString();
        assertTrue(stringRepresentation.contains("Alice: Hello, Bob!"), "toString should contain sender and text.");
        assertTrue(stringRepresentation.startsWith("["), "toString should start with timestamp bracket.");
    }
}
