package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    /**
     * Returns all known tags from the tags table, sorted alphabetically.
     */
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        String sql = "SELECT name FROM tags ORDER BY name";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tags.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tags.");
            e.printStackTrace();
        }
        return tags;
    }

    /**
     * Takes a raw list of tag strings, normalizes each one (lowercase + trim),
     * inserts it into the tags table if it doesn't already exist (ON CONFLICT DOES NOTHING),
     * and returns the list of normalized tag names that were processed.
     *
     * This means that tahs like for exmpl:"Java", "JAVA", and "java" all resolve to the same "java" row.
     */
    public List<String> upsertTags(List<String> rawTags) {
        List<String> normalized = new ArrayList<>();
        if (rawTags == null || rawTags.isEmpty()) {
            return normalized;
        }

        String sql = "INSERT INTO tags (name) VALUES (?) ON CONFLICT (name) DO NOTHING";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String raw : rawTags) {
                String clean = raw.toLowerCase().trim();
                if (clean.isEmpty()) {
                    continue;
                }
                normalized.add(clean);
                pstmt.setString(1, clean);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Error upserting tags.");
            e.printStackTrace();
        }
        return normalized;
    }
}
