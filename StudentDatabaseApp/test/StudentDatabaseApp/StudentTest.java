package StudentDatabaseApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for the pure logic in Student: IDs, tuition math, enrollment. */
public class StudentTest {

	@Test
	void studentIdIsGradeYearFollowedByRunningNumber() {
		assertEquals("21001", Student.generateStudentID(2, 1001));
		assertEquals("11002", Student.generateStudentID(1, 1002));
		assertEquals("41005", Student.generateStudentID(4, 1005));
	}

	@Test
	void enrollingInACourseCosts600() {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 0);
		student.addCourse("History 101");
		assertEquals(600, student.getTuitionBalance());
		assertTrue(student.getEnrolledCourses().contains("History 101"));
		assertTrue(student.getCourses().contains("History 101"));
	}

	@Test
	void enrollingInThreeCoursesCosts1800() {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 0);
		student.addCourse("History 101");
		student.addCourse("Mathematics 101");
		student.addCourse("English 101");
		assertEquals(1800, student.getTuitionBalance());
		assertEquals(3, student.getEnrolledCourses().size());
	}

	@Test
	void paymentReducesTheBalance() {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 1200);
		student.makePayment(600);
		assertEquals(600, student.getTuitionBalance());
	}

	@Test
	void payingInFullLeavesZeroBalance() {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 600);
		student.makePayment(600);
		assertEquals(0, student.getTuitionBalance());
	}

	@Test
	void dataConstructorKeepsFieldsAndCourses() {
		Student student = new Student("Grace", "Hopper", 4, "41001",
				"\n Computer Science 101\n Chemistry 101", 1200);
		assertEquals("Grace", student.getFirstName());
		assertEquals("Hopper", student.getLastName());
		assertEquals(4, student.getGradeYear());
		assertEquals("41001", student.getStudentID());
		assertEquals(1200, student.getTuitionBalance());
		assertEquals(2, student.getEnrolledCourses().size());
		// Courses loaded from the database are not "new" courses to save.
		assertTrue(student.getNewCourses().isEmpty());
	}

	@Test
	void toStringShowsNameIdCoursesAndBalance() {
		Student student = new Student("Ada", "Lovelace", 2, "21001", "", 0);
		student.addCourse("History 101");
		String text = student.toString();
		assertTrue(text.contains("Ada Lovelace"));
		assertTrue(text.contains("21001"));
		assertTrue(text.contains("History 101"));
		assertTrue(text.contains("Balance: $600"));
	}
}
