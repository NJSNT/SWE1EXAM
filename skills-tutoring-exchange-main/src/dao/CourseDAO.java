package dao;

import model.Course;
import model.Student;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourseDAO {

    private final TagDAO tagDAO = new TagDAO();

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

    /**
     * Splits a raw comma-separated tag string into individual tags,
     * upserts them into the tags table (normalizing case in the process),
     * and returns the normalized comma-separated string to store on the course.
     */
    private String normalizeAndUpsertTags(String rawTagString) {
        if (rawTagString == null || rawTagString.trim().isEmpty()) {
            return "";
        }
        List<String> rawList = Arrays.asList(rawTagString.split(","));
        List<String> normalized = tagDAO.upsertTags(rawList);
        return normalized.stream().collect(Collectors.joining(", "));
    }

    public boolean addCourse(Course course) {
        // Normalize tags and register any new ones in the tags table before saving the course.
        String normalizedTags = normalizeAndUpsertTags(course.getTags());
        course.setTags(normalizedTags);

        String sql = "INSERT INTO courses (name, information, tags, tutor_id) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int tutorId = getUserIdByName(course.getTutor().getName());
            if (tutorId == -1) return false;

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getInformation());
            pstmt.setString(3, course.getTags());
            pstmt.setInt(4, tutorId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding course.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.information, c.tags, u.name AS tutor_name " +
                     "FROM courses c JOIN users u ON c.tutor_id = u.id";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student dummyTutor = new Student(rs.getString("tutor_name"), "");
                Course course = new Course(rs.getString("name"), rs.getString("information"), rs.getString("tags"), dummyTutor);
                course.setId(rs.getInt("id"));
                courses.add(course);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching courses.");
            e.printStackTrace();
        }
        return courses;
    }

    public boolean enrollStudent(Student student, Course course) {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int studentId = getUserIdByName(student.getName());
            int courseId = course.getId();
            if (courseId == -1) {
                courseId = getCourseId(course.getName(), course.getTutor().getName());
            }

            if (studentId == -1 || courseId == -1) return false;

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error enrolling student.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Course> getRegisteredCourses(User currentUser) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.information, c.tags, tutor.name AS tutor_name " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.id " +
                "JOIN users tutor ON c.tutor_id = tutor.id " +
                "JOIN users student ON e.student_id = student.id " +
                "WHERE student.name = ? " +
                "ORDER BY c.name";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser.getName());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Student dummyTutor = new Student(rs.getString("tutor_name"), "");
                    Course course = new Course(rs.getString("name"), rs.getString("information"), rs.getString("tags"), dummyTutor);
                    course.setId(rs.getInt("id"));
                    courses.add(course);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching registered courses.");
            e.printStackTrace();
        }
        return courses;
    }

    public boolean cancelRegistration(Course course, User currentUser) {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int studentId = getUserIdByName(currentUser.getName());
            if (studentId == -1 || course.getId() == -1) return false;

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, course.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error canceling registration.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCourse(Course course, User currentUser) {
        String sql = "DELETE FROM courses WHERE id = ? AND tutor_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int tutorId = getUserIdByName(currentUser.getName());
            if (course.getId() == -1 || tutorId == -1) return false;

            pstmt.setInt(1, course.getId());
            pstmt.setInt(2, tutorId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting course.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCourseAsAdmin(Course course) {
        if (course == null || course.getId() == -1) return false;

        String sql = "DELETE FROM courses WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, course.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting course as admin.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCourse(Course course, User currentUser) {
        // Normalize tags and register any new ones in the tags table before saving.
        String normalizedTags = normalizeAndUpsertTags(course.getTags());
        course.setTags(normalizedTags);

        String sql = "UPDATE courses SET name = ?, information = ?, tags = ? WHERE id = ? AND tutor_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int tutorId = getUserIdByName(currentUser.getName());
            if (course.getId() == -1 || tutorId == -1) return false;

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getInformation());
            pstmt.setString(3, course.getTags());
            pstmt.setInt(4, course.getId());
            pstmt.setInt(5, tutorId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating course.");
            e.printStackTrace();
            return false;
        }
    }

    private int getCourseId(String courseName, String tutorName) throws SQLException {
        String sql = "SELECT c.id FROM courses c JOIN users u ON c.tutor_id = u.id " +
                     "WHERE c.name = ? AND u.name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseName);
            pstmt.setString(2, tutorName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
}
