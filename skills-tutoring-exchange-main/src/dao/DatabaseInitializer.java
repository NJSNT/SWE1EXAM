package dao;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Cannot initialize database: Connection is null.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {

            // 1. Users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_type VARCHAR(20) NOT NULL, " +
                    "name VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "tags TEXT" +
                    ")";
            stmt.execute(createUsersTable);
            System.out.println("Table 'users' verified/created.");

            stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS tags TEXT");

            try {
                stmt.execute("ALTER TABLE users ADD CONSTRAINT unique_name UNIQUE (name)");
            } catch (Exception e) {
                // Constraint already exists — safe to ignore.
            }

            stmt.execute(
                    "INSERT INTO users (user_type, name, password, tags) " +
                    "SELECT 'Administrator', 'admin', 'admin', '' " +
                    "WHERE NOT EXISTS (SELECT 1 FROM users WHERE name = 'admin' AND user_type = 'Administrator')"
            );

            // 2. Courses table
            String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "information TEXT, " +
                    "tags TEXT, " +
                    "tutor_id INT, " +
                    "FOREIGN KEY (tutor_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createCoursesTable);
            System.out.println("Table 'courses' verified/created.");
            stmt.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS tags TEXT");

            // 3. Enrollments table
            String createEnrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (" +
                    "student_id INT, " +
                    "course_id INT, " +
                    "PRIMARY KEY (student_id, course_id), " +
                    "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createEnrollmentsTable);
            System.out.println("Table 'enrollments' verified/created.");

            // 4. Messages table
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id SERIAL PRIMARY KEY, " +
                    "sender_id INT, " +
                    "receiver_id INT, " +
                    "text TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createMessagesTable);
            System.out.println("Table 'messages' verified/created.");
            stmt.execute(
                    "ALTER TABLE messages " +
                    "ADD COLUMN IF NOT EXISTS receiver_id INT REFERENCES users(id) ON DELETE CASCADE"
            );

            // 5. Tags table
            String createTagsTable = "CREATE TABLE IF NOT EXISTS tags (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(100) UNIQUE NOT NULL" +
                    ")";
            stmt.execute(createTagsTable);
            System.out.println("Table 'tags' verified/created.");

            stmt.execute(
                    "INSERT INTO tags (name) " +
                    "SELECT tag FROM (" +
                    "  SELECT DISTINCT LOWER(TRIM(unnest(string_to_array(tags, ',')))) AS tag " +
                    "  FROM courses WHERE tags IS NOT NULL AND tags <> ''" +
                    ") t WHERE tag <> '' ON CONFLICT (name) DO NOTHING"
            );
            stmt.execute(
                    "INSERT INTO tags (name) " +
                    "SELECT tag FROM (" +
                    "  SELECT DISTINCT LOWER(TRIM(unnest(string_to_array(tags, ',')))) AS tag " +
                    "  FROM users WHERE tags IS NOT NULL AND tags <> ''" +
                    ") t WHERE tag <> '' ON CONFLICT (name) DO NOTHING"
            );

            // 6. Notifications table
            // Notifications are saved here when created. delivered=FALSE means the recipient
            // hasn't received it yet (they were offline). On login, all pending rows are pushed
            // to the client and marked delivered=TRUE. The row stays in the DB until the user
            // explicitly clears or dismisses the notification, so it survives app restarts.
            String createNotificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                    "id SERIAL PRIMARY KEY, " +
                    "recipient_name VARCHAR(100) NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "related_user VARCHAR(100) DEFAULT '', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "delivered BOOLEAN DEFAULT FALSE" +
                    ")";
            stmt.execute(createNotificationsTable);
            System.out.println("Table 'notifications' verified/created.");

            System.out.println("Database initialization completed successfully.");

        } catch (Exception e) {
            System.err.println("Error initializing database tables.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
