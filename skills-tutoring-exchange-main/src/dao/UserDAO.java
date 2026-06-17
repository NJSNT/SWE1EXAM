package dao;

import model.Administrator;
import model.Student;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Method to create/register a new user
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (user_type, name, password, tags) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Determine user type
            String userType = user instanceof Administrator ? "Administrator" : "Student";
            
            pstmt.setString(1, userType);
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getTags());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting user.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUsernameExists(String name) {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence.");
            e.printStackTrace();
        }
        return false;
    }

    // Method to authenticate a user
    public User authenticateUser(String name, String password) {
        String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userType = rs.getString("user_type");
                    User user;
                    if ("Administrator".equals(userType)) {
                        user = new Administrator(rs.getString("name"), rs.getString("password"));
                    } else {
                        user = new Student(rs.getString("name"), rs.getString("password"));
                    }
                    // Tags are loaded with the user so matching can happen in the dashboard
                    user.setTags(rs.getString("tags"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user.");
            e.printStackTrace();
        }
        return null; // Return null if authentication fails
    }

    public boolean updateUserTags(User user) {
        String sql = "UPDATE users SET tags = ? WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getTags());
            pstmt.setString(2, user.getName());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user tags.");
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_type, name, tags FROM users ORDER BY user_type, name";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user;
                // The admin screen does not need to know user passwords.
                if ("Administrator".equals(rs.getString("user_type"))) {
                    user = new Administrator(rs.getString("name"), "");
                } else {
                    user = new Student(rs.getString("name"), "");
                }
                user.setTags(rs.getString("tags"));
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching users.");
            e.printStackTrace();
        }
        return users;
    }

    // Method to delete a user by name (useful for Admin)
    public boolean deleteUser(String name) {
        String sql = "DELETE FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean isAdministrator(String name) {
        String sql = "SELECT user_type FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "Administrator".equals(rs.getString("user_type"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking user role.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean promoteStudentToAdmin(String name) {
        String sql = "UPDATE users SET user_type = 'Administrator' WHERE name = ? AND user_type = 'Student'";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error promoting user to admin.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean demoteAdminToStudent(String name) {
        String sql = "UPDATE users SET user_type = 'Student' WHERE name = ? AND user_type = 'Administrator'";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error demoting user to student.");
            e.printStackTrace();
            return false;
        }
    }
}
