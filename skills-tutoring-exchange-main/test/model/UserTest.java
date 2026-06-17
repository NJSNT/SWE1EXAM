package model;

import model.Student;
import model.Notification;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        // Since User is abstract, we test it through Student
        user = new Student("TestUser", "password123");
    }

    // 1. ZOMBIES - Boundaries & Nulls: Testing tags boundary conditions
    @Test
    public void testSetTagsWithNull() {
        user.setTags(null);
        assertEquals("", user.getTags(), "Tags should be initialized to an empty string when null is passed.");
    }

    @Test
    public void testSetTagsWithEmptyString() {
        user.setTags("");
        assertEquals("", user.getTags(), "Tags should be an empty string.");
    }

    @Test
    public void testSetTagsWithSpaces() {
        user.setTags("  java, sql  ");
        assertEquals("java, sql", user.getTags(), "Tags should be trimmed.");
    }

    // 2. ZOMBIES - Zero/One/Many: Testing notifications
    @Test
    public void testReceiveZeroNotifications() {
        assertTrue(user.getNotifications().isEmpty(), "Notifications list should be empty initially.");
    }

    @Test
    public void testReceiveOneNotification() {
        Notification notification = new Notification("Test Subject", "Test Message", null);
        user.receiveNotification(notification);
        
        assertEquals(1, user.getNotifications().size(), "Notifications list should have 1 item.");
        assertTrue(user.getNotifications().contains(notification), "Notifications list should contain the added notification.");
    }

    @Test
    public void testReceiveManyNotifications() {
        user.receiveNotification(new Notification("Subject 1", "Msg 1", null));
        user.receiveNotification(new Notification("Subject 2", "Msg 2", null));
        user.receiveNotification(new Notification("Subject 3", "Msg 3", null));
        
        assertEquals(3, user.getNotifications().size(), "Notifications list should have 3 items.");
    }
}
