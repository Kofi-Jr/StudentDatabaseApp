package StudentDatabaseApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/*
 * Student Database Application
 *
 * Scenario: You are a Database Administrator for a university and
 * need to create an application to manage student enrollments and balance.
 *
 * Your application should do the following:
 * 	Ask the user how many students will be added to the database
 * 	The user should be prompted to enter the name and year for each student
 * 	The student should have a 5-digit unique ID, with the first number being their grade level
 * 	A student can enroll in the following courses:
 *
 * 	History 101
 * 	Mathematics 101
 * 	English 101
 * 	Chemistry 101
 * 	Computer Science 101
 *
 * 	Each course costs $600 to enroll
 * 	The student should be able to view their balance and pay the tuition
 * 	To see the status of the student, we should see their name, ID courses enrolled, and balance.
 *
 */

public class Student {
	private String firstName;
	private String lastName;
	private int gradeYear;
	private String studentID;
	private String courses = "";
	private int tuitionBalance = 0;
	private static int costOfCourse = 600;
	private static int id = 1000;

	// Courses this student enrolled in, in order. Kept in step with the
	// display string above so the database layer can save them one by one.
	private final List<String> courseList = new ArrayList<>();
	// Courses added during this run (empty for students loaded from the database).
	private final List<String> newCourses = new ArrayList<>();

	// The single Scanner for console input, supplied by the caller.
	// It is never created here and never closed here; main() owns it.
	private Scanner input;

	/** Interactive constructor: prompt the user for name and class level. */
	public Student(Scanner in) {
		this(in, nextLocalId());
	}

	/**
	 * Interactive constructor with an explicit ID number. The main program
	 * passes the next number from the database so IDs never collide across
	 * runs; the plain constructor above falls back to the in-memory counter.
	 */
	public Student(Scanner in, int nextIdNumber) {
		this.input = in;

		System.out.print("Enter student first name: ");
		this.firstName = in.nextLine();

		System.out.print("Enter student last name: ");
		this.lastName = in.nextLine();

		System.out.print("1 - Freshman\n2 - Sophomore\n3 - "
				+ "Junior\n4 - "
				+ "Senior\nEnter student class level:  ");
		this.gradeYear = in.nextInt();
		in.nextLine(); // consume the newline left by nextInt()

		this.studentID = generateStudentID(gradeYear, nextIdNumber);
	}

	/**
	 * Data constructor: build a student from values already known, e.g.
	 * loaded from the database or created in a test. Does not prompt.
	 */
	public Student(String firstName, String lastName, int gradeYear,
			String studentID, String courses, int tuitionBalance) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gradeYear = gradeYear;
		this.studentID = studentID;
		this.courses = courses;
		this.tuitionBalance = tuitionBalance;
		if (courses != null) {
			for (String course : courses.split("\n")) {
				String trimmed = course.trim();
				if (!trimmed.isEmpty()) {
					courseList.add(trimmed);
				}
			}
		}
	}

	/** Student ID: the grade year followed by the running number, e.g. 21001. */
	public static String generateStudentID(int gradeYear, int nextNumber) {
		return gradeYear + "" + nextNumber;
	}

	/** Next number from the in-memory counter (used when no database is available). */
	private static synchronized int nextLocalId() {
		return ++id;
	}

	public static int getCostOfCourse() {
		return costOfCourse;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public int getGradeYear() {
		return gradeYear;
	}

	public String getStudentID() {
		return studentID;
	}

	public String getCourses() {
		return courses;
	}

	public int getTuitionBalance() {
		return tuitionBalance;
	}

	/** Courses this student is enrolled in, in enrollment order. */
	public List<String> getEnrolledCourses() {
		return Collections.unmodifiableList(courseList);
	}

	/** Courses enrolled in during this run (not yet saved to the database). */
	public List<String> getNewCourses() {
		return Collections.unmodifiableList(newCourses);
	}

	/** Called by the database layer after the new courses have been saved. */
	void clearNewCourses() {
		newCourses.clear();
	}

	// Enroll in a single course: adds it and charges the course cost.
	public void addCourse(String course) {
		courses += "\n " + course;
		courseList.add(course);
		newCourses.add(course);
		tuitionBalance += costOfCourse;
	}

	// Enroll in courses
	public void enroll() {
		do {
			System.out.print("Enter course to enroll (Q to quit): ");
			String course = input.nextLine();
			if (!course.equals("Q")) {
				addCourse(course);
			} else {
				break;
			}
		} while (1 != 0);
	}

	// View balance
	public void viewBalance() {
		System.out.println("Your balance is: $" + tuitionBalance);

	}

	// Pay part of the tuition balance.
	public void makePayment(int payment) {
		tuitionBalance -= payment;
	}

	// Pay Tuition
	public void payTuition() {
		viewBalance();
		System.out.print("Enter your payment: $");
		int payment = input.nextInt();
		input.nextLine(); // consume the newline left by nextInt()
		makePayment(payment);
		System.out.println("Thank you for your payment of $" + payment);
		viewBalance();
	}

	// Show status
	public String toString() {
		return "Name: " + firstName + " " + lastName +
				"\nGrade Level: " + gradeYear +
				"\nStudent ID: " + studentID +
				"\nCourses Enrolled: " + courses +
				"\nBalance: $" + tuitionBalance;
	}

}
