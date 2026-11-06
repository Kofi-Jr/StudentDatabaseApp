package StudentDatabaseApp;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*
 * Database: the MySQL persistence layer for the Student Database App.
 *
 * Configuration (no password is ever hard-coded here):
 *   - Environment variables DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
 *   - or a db.properties file in the working directory (see db.properties.example)
 *   - otherwise the defaults below (local MySQL, database student_db)
 *
 * The app still runs without a database: StudentDatabaseApp checks
 * Database.isAvailable() first and falls back to in-memory mode.
 */
public class Database {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_NAME = "student_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private final Connection connection;

    /** Connect using the environment / db.properties configuration. */
    public Database() throws SQLException {
        this(buildUrl(), configValue("DB_USER", "db.user", DEFAULT_USER),
                configValue("DB_PASSWORD", "db.password", DEFAULT_PASSWORD));
    }

    /** Connect to a specific database (used by tests). */
    public Database(String url, String user, String password) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J driver not found on the classpath. "
                    + "Download mysql-connector-j and add it to the classpath (see README).", e);
        }
        this.connection = DriverManager.getConnection(url, user, password);
        ensureTables();
    }

    /** True when a database connection can be opened with the current config. */
    public static boolean isAvailable() {
        try (Connection c = DriverManager.getConnection(buildUrl(),
                configValue("DB_USER", "db.user", DEFAULT_USER),
                configValue("DB_PASSWORD", "db.password", DEFAULT_PASSWORD))) {
            return c.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private static String buildUrl() {
        String host = configValue("DB_HOST", "db.host", DEFAULT_HOST);
        String port = configValue("DB_PORT", "db.port", DEFAULT_PORT);
        String name = configValue("DB_NAME", "db.name", DEFAULT_NAME);
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    }

    /** Environment variable wins, then db.properties, then the default. */
    private static String configValue(String envKey, String propertyKey, String defaultValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.isEmpty()) {
            return env;
        }
        Properties props = loadProperties();
        String prop = props.getProperty(propertyKey);
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }
        return defaultValue;
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (Exception ignored) {
            // No db.properties file: fall back to environment variables / defaults.
        }
        return props;
    }

    /** Create the tables if they do not exist yet (same as schema.sql). */
    public void ensureTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "first_name VARCHAR(100) NOT NULL, "
                    + "last_name VARCHAR(100) NOT NULL, "
                    + "grade_year INT NOT NULL, "
                    + "student_id VARCHAR(10) NOT NULL UNIQUE, "
                    + "tuition_balance INT NOT NULL DEFAULT 0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS enrollments ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "student_id VARCHAR(10) NOT NULL, "
                    + "course_name VARCHAR(100) NOT NULL, "
                    + "cost INT NOT NULL, "
                    + "FOREIGN KEY (student_id) REFERENCES students (student_id))");
        }
    }

    /**
     * The next ID number to use. Student IDs are the grade year followed by
     * a running number (e.g. 21001). The number comes from the database, so
     * it survives restarts and can never collide with an existing student.
     * Returns 1001 when the students table is empty.
     */
    public int nextStudentNumber() throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(student_id, 2) AS UNSIGNED)), 1000) "
                + "FROM students";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1) + 1;
        }
    }

    /** Insert a student, or update their balance if the student ID exists. */
    public void saveStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (first_name, last_name, grade_year, student_id, tuition_balance) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE tuition_balance = VALUES(tuition_balance)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setInt(3, student.getGradeYear());
            ps.setString(4, student.getStudentID());
            ps.setInt(5, student.getTuitionBalance());
            ps.executeUpdate();
        }
    }

    /** Save the courses a student enrolled in during this run. */
    public void saveNewEnrollments(Student student) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_name, cost) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String course : student.getNewCourses()) {
                ps.setString(1, student.getStudentID());
                ps.setString(2, course);
                ps.setInt(3, Student.getCostOfCourse());
                ps.executeUpdate();
            }
        }
        student.clearNewCourses();
    }

    /** Persist a student's current tuition balance (e.g. after a payment). */
    public void updateBalance(Student student) throws SQLException {
        String sql = "UPDATE students SET tuition_balance = ? WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, student.getTuitionBalance());
            ps.setString(2, student.getStudentID());
            ps.executeUpdate();
        }
    }

    /** Load every student saved in the database, with their enrollments. */
    public List<Student> loadAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT first_name, last_name, grade_year, student_id, tuition_balance "
                + "FROM students ORDER BY id";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String studentID = rs.getString("student_id");
                students.add(new Student(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("grade_year"),
                        studentID,
                        loadCoursesText(studentID),
                        rs.getInt("tuition_balance")));
            }
        }
        return students;
    }

    private String loadCoursesText(String studentID) throws SQLException {
        String sql = "SELECT course_name FROM enrollments WHERE student_id = ? ORDER BY id";
        StringBuilder courses = new StringBuilder();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, studentID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.append("\n ").append(rs.getString("course_name"));
                }
            }
        }
        return courses.toString();
    }

    public void close() throws SQLException {
        connection.close();
    }
}
