package dao;

import model.Message;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private int getUserIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    private void ensureReceiverColumn() throws SQLException {
        String sql = "ALTER TABLE messages " +
                "ADD COLUMN IF NOT EXISTS receiver_id INT REFERENCES users(id) ON DELETE CASCADE";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, text, timestamp) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            ensureReceiverColumn();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
                int senderId = getUserIdByName(message.getSender().getName());
                if (senderId == -1) return false;

                int receiverId = -1;
                if (message.getReceiver() != null) {
                    receiverId = getUserIdByName(message.getReceiver().getName());
                    if (receiverId == -1) return false;
                }

                pstmt.setInt(1, senderId);
                if (receiverId == -1) {
                    pstmt.setNull(2, Types.INTEGER);
                } else {
                    pstmt.setInt(2, receiverId);
                }
                pstmt.setString(3, message.getText());
                pstmt.setTimestamp(4, Timestamp.valueOf(message.getTimeStamp()));
            
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving message.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getDirectChatHistory(model.User user, model.User chatPartner) {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT m.text, m.timestamp, sender.name AS sender_name, receiver.name AS receiver_name " +
                "FROM messages m " +
                "JOIN users sender ON m.sender_id = sender.id " +
                "JOIN users receiver ON m.receiver_id = receiver.id " +
                "WHERE (sender.name = ? AND receiver.name = ?) " +
                "OR (sender.name = ? AND receiver.name = ?) " +
                "ORDER BY m.timestamp ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            ensureReceiverColumn();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, chatPartner.getName());
                pstmt.setString(3, chatPartner.getName());
                pstmt.setString(4, user.getName());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Student dummySender = new Student(rs.getString("sender_name"), "");
                        Student dummyReceiver = new Student(rs.getString("receiver_name"), "");
                        Message msg = new Message(dummySender, dummyReceiver, rs.getString("text"));
                        msg.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());
                        history.add(msg);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching direct chat history.");
            e.printStackTrace();
        }
        return history;
    }

    public List<model.User> getChatPartners(model.User user) {
        List<model.User> partners = new ArrayList<>();
        String sql = "SELECT DISTINCT partner_name FROM (" +
                "SELECT receiver.name AS partner_name " +
                "FROM messages m " +
                "JOIN users sender ON m.sender_id = sender.id " +
                "JOIN users receiver ON m.receiver_id = receiver.id " +
                "WHERE sender.name = ? " +
                "UNION " +
                "SELECT sender.name AS partner_name " +
                "FROM messages m " +
                "JOIN users sender ON m.sender_id = sender.id " +
                "JOIN users receiver ON m.receiver_id = receiver.id " +
                "WHERE receiver.name = ?" +
                ") chat_partners ORDER BY partner_name";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            ensureReceiverColumn();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getName());

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        partners.add(new Student(rs.getString("partner_name"), ""));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching chat partners.");
            e.printStackTrace();
        }
        return partners;
    }
}
