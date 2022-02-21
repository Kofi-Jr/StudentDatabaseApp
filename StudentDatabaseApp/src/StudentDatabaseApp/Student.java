package StudentDatabaseApp;


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
	
	// Constructor: prompt user to enter student's name and year
	public Student() {
		Scanner in = new Scanner(System.in);
		System.out.print("Enter student first name: ");
		this.firstName = in.nextLine();
		
		System.out.print("Enter student last name: ");
		this.lastName = in.nextLine();
		
		System.out.print("1 - Freshman\n2 - Sophmore\n3 - "
				+ "Junior\n4 - "
				+ "Senior\nEnter student class level:  ");
		this.gradeYear = in.nextInt();
		
		setStudentID();
		
	}
	
	// Generate an ID
	private void setStudentID() {
		id++;
		// Grade level + ID
		this.studentID = gradeYear + "" + id;
	}
	
	
	// Enroll in courses
	public void enroll() {
		do {
			System.out.print("Enter course to enroll (Q to quit): ");
			Scanner in = new Scanner(System.in);
			String course = in.nextLine();
			if(!course.equals( "Q")) {
				courses += "\n " + course;
				tuitionBalance += costOfCourse;
			} 
			else { 
				break; 
			}
		} while(1 != 0);	
	} 
	
	
	// View balance 
	public void viewBalance() {
		System.out.println("Your balance is: $" + tuitionBalance);
		
	}
	
	// Pay Tuition
	public void payTuition() {
		viewBalance();
		System.out.print("Enter your payment: $");
		Scanner in = new Scanner(System.in);
		int payment = in.nextInt();	
		tuitionBalance -= payment;	
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

