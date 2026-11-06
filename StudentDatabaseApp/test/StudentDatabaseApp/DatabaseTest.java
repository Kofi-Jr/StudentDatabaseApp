package StudentDatabaseApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the MySQL persistence layer.
 *
 * Needs a running MySQL server. It uses its own test database
 * (student_db_test) so it never touches the real student_db data.
 * Configure it with DB_TEST_URL / DB_USER / DB_PASSWORD, or let it use
 * the local defaults (localhost:3306, root, no password). The tests are
 * skipped when no database is reachable.
 */
public class DatabaseTest {

	private static final String TEST_URL = System.getenv().getOrDefault("DB_TEST_URL",
			"jdbc:mysql://localhost:3306/student_db_test?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC");
	private static final String TEST_USER = System.getenv().getOrDefault("DB_USER", "root");
	private static final String TEST_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

	private static Database db;
	private static boolean available;

	@BeforeAll
	static void connect() throws Exception {
		try {
			try (Connection admin = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
					TEST_USER, TEST_PASSWORD);
					Statement st = admin.createStatement()) {
				st.executeUpdate("CREATE DATABASE IF NOT EXISTS student_db_test");
			}
		} catch (Exception ignored) {
			// The test database may already exist, or this user may not be
			// allowed to create databases. Try connecting anyway.
		}
		try {
			db = new Database(TEST_URL, TEST_USER, TEST_PASSWORD);
			available = true;
		} catch (Exception e) {
			available = false;
		}
		assumeTrue(available, "Skipped: no MySQL server reachable at " + TEST_URL);
	}

	@AfterAll
	static void close() throws Exception {
		if (db != null) {
			db.close();
		}
	}

	@BeforeEach
	void cleanTables() throws Exception {
		try (Connection c = DriverManager.getConnection(TEST_URL, TEST_USER, TEST_PASSWORD);
				Statement st = c.createStatement()) {
			st.executeUpdate("DELETE FROM enrollments");
			st.executeUpdate("DELETE FROM students");
		}
	}

	@Test
	void nextStudentNumberStartsAt1001OnAnEmptyTable() throws Exception {
		assertEquals(1001, db.nextStudentNumber());
	}

	@Test
	void saveAndLoadRoundTripKeepsStudentAndCourses() throws Exception {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 0);
		student.addCourse("History 101");
		student.addCourse("Mathematics 101");
		db.saveStudent(student);
		db.saveNewEnrollments(student);

		List<Student> loaded = db.loadAllStudents();
		assertEquals(1, loaded.size());
		Student found = loaded.get(0);
		assertEquals("Ada", found.getFirstName());
		assertEquals("Lovelace", found.getLastName());
		assertEquals(2, found.getGradeYear());
		assertEquals("21001", found.getStudentID());
		assertEquals(1200, found.getTuitionBalance());
		assertEquals(2, found.getEnrolledCourses().size());
		assertTrue(found.getEnrolledCourses().contains("History 101"));
		assertTrue(found.getEnrolledCourses().contains("Mathematics 101"));
	}

	@Test
	void nextStudentNumberFollowsTheHighestSavedId() throws Exception {
		db.saveStudent(new Student("Ada", "Lovelace", 2, "21001", "", 0));
		db.saveStudent(new Student("Grace", "Hopper", 4, "41002", "", 0));
		assertEquals(1003, db.nextStudentNumber());
	}

	@Test
	void balanceUpdateIsPersisted() throws Exception {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 1200);
		db.saveStudent(student);
		student.makePayment(600);
		db.updateBalance(student);

		List<Student> loaded = db.loadAllStudents();
		assertEquals(1, loaded.size());
		assertEquals(600, loaded.get(0).getTuitionBalance());
	}
}
