package dao;

import model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    /**
     * Saves a notification to the database for a specific recipient.
     * The generated DB id is written back onto the notification object so
     * the caller can use it for later deletion (dismiss / delivered tracking).
     */
    public void save(String recipientName, Notification notification) {
        String sql = "INSERT INTO notifications (recipient_name, title, message, related_user, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, recipientName);
            pstmt.setString(2, notification.getTitle());
            pstmt.setString(3, notification.getMessageInformation());
            pstmt.setString(4, notification.getRelatedUserName());
            pstmt.setTimestamp(5, Timestamp.valueOf(notification.getCreatedAt()));
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving notification.");
            e.printStackTrace();
        }
    }

    /**
     * Returns all undelivered notifications for the given user, oldest first.
     * These are notifications that were created while the user was offline.
     */
    public List<Notification> getPendingForUser(String username) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT id, title, message, related_user, created_at " +
                     "FROM notifications " +
                     "WHERE recipient_name = ? AND delivered = FALSE " +
                     "ORDER BY created_at ASC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification(
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getString("related_user")
                    );
                    n.setId(rs.getInt("id"));
                    n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending notifications.");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Marks a single notification as delivered so it won't be re-sent on the next login.
     */
    public void markDelivered(int id) {
        String sql = "UPDATE notifications SET delivered = TRUE WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking notification delivered.");
            e.printStackTrace();
        }
    }

    /**
     * Marks all pending notifications for a user as delivered.
     * Called after pushing the full backlog to a user who just logged in.
     */
    public void markAllDeliveredForUser(String username) {
        String sql = "UPDATE notifications SET delivered = TRUE WHERE recipient_name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking all notifications delivered.");
            e.printStackTrace();
        }
    }

    /**
     * Permanently deletes a single notification (user dismissed it).
     */
    public void deleteById(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting notification by id.");
            e.printStackTrace();
        }
    }

    /**
     * Permanently deletes all notifications for a user (user cleared all).
     */
    public void deleteAllForUser(String username) {
        String sql = "DELETE FROM notifications WHERE recipient_name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing notifications for user.");
            e.printStackTrace();
        }
    }
}
