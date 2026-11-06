package StudentDatabaseApp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentDatabaseApp {

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		// Connect to MySQL if one is available. Without a database the app
		// still runs exactly as before, keeping students in memory only.
		Database db = null;
		try {
			if (Database.isAvailable()) {
				db = new Database();
				System.out.println("Connected to the MySQL database.");
			}
		} catch (SQLException e) {
			System.out.println("Warning: could not connect to the MySQL database: " + e.getMessage());
		}
		if (db == null) {
			System.out.println("Running without database persistence.");
		}

		// Students already saved in the database are loaded first.
		List<Student> allStudents = new ArrayList<>();
		if (db != null) {
			try {
				allStudents.addAll(db.loadAllStudents());
			} catch (SQLException e) {
				System.out.println("Warning: could not load saved students: " + e.getMessage());
			}
		}

		// Ask how many new students we want to add
		System.out.print("Enter number of new students to enroll: ");
		int numOfStudents = in.nextInt();
		in.nextLine(); // consume the newline left by nextInt()
		List<Student> newStudents = new ArrayList<>();

		// Create n number of new students
		for (int n = 0; n < numOfStudents; n++) {
			Student student;
			if (db != null) {
				try {
					student = new Student(in, db.nextStudentNumber());
				} catch (SQLException e) {
					System.out.println("Warning: could not get the next student ID from the database: " + e.getMessage());
					student = new Student(in);
				}
			} else {
				student = new Student(in);
			}
			student.enroll();
			if (db != null) {
				try {
					db.saveStudent(student);
					db.saveNewEnrollments(student);
				} catch (SQLException e) {
					System.out.println("Warning: could not save student to the database: " + e.getMessage());
				}
			}
			student.payTuition();
			if (db != null) {
				try {
					db.updateBalance(student);
				} catch (SQLException e) {
					System.out.println("Warning: could not save the new balance: " + e.getMessage());
				}
			}
			newStudents.add(student);
		}

		allStudents.addAll(newStudents);
		for (Student student : allStudents) {
			System.out.println(student.toString());
		}

		if (db != null) {
			try {
				db.close();
			} catch (SQLException ignored) {
				// Nothing to do at shutdown.
			}
		}
		in.close();
	}

}
