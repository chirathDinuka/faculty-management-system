package com.faculty.dao;

import com.faculty.model.Student;
import com.faculty.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDAO {

    // READ: Get all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT s.*, d.name AS degree_name FROM students s LEFT JOIN degrees d ON s.degree_id = d.id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"), rs.getInt("user_id"), rs.getString("student_reg_id"),
                        rs.getString("full_name"), rs.getString("email"),
                        rs.getString("mobile_number"), rs.getInt("degree_id"), rs.getString("degree_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Helper: Get Degrees for the dropdown menu
    public Map<String, Integer> getDegreeOptions() {
        Map<String, Integer> degrees = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM degrees");
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                degrees.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return degrees;
    }

    // CREATE: Add a new student and their user account
    public boolean addStudent(Student student, String defaultPassword) {
        String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'Student')";
        String insertStudent = "INSERT INTO students (user_id, student_reg_id, full_name, email, mobile_number, degree_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement studentStmt = conn.prepareStatement(insertStudent)) {

                // 1. Create User
                userStmt.setString(1, student.getStudentRegId()); // Use RegID as username
                userStmt.setString(2, defaultPassword);
                userStmt.executeUpdate();

                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // 2. Create Student
                    studentStmt.setInt(1, userId);
                    studentStmt.setString(2, student.getStudentRegId());
                    studentStmt.setString(3, student.getFullName());
                    studentStmt.setString(4, student.getEmail());
                    studentStmt.setString(5, student.getMobileNumber());
                    studentStmt.setInt(6, student.getDegreeId());
                    studentStmt.executeUpdate();
                }
                conn.commit(); // Save changes
                return true;
            } catch (SQLException ex) {
                conn.rollback(); // Cancel if error occurs
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // UPDATE: Edit existing student
    public boolean updateStudent(Student student) {
        String query = "UPDATE students SET full_name=?, email=?, mobile_number=?, degree_id=? WHERE student_reg_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, student.getFullName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getMobileNumber());
            stmt.setInt(4, student.getDegreeId());
            stmt.setString(5, student.getStudentRegId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE: Delete student (will also delete their user account due to CASCADE)
    public boolean deleteStudent(String studentRegId) {
        String query = "DELETE users FROM users JOIN students ON users.id = students.user_id WHERE students.student_reg_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentRegId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- Helper: Get student by ID ----------
    public Student getStudentById(int id) {
        String query = "SELECT s.*, d.name AS degree_name FROM students s " +
                "LEFT JOIN degrees d ON s.degree_id = d.id " +
                "WHERE s.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("student_reg_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("mobile_number"),
                        rs.getInt("degree_id"),
                        rs.getString("degree_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- Get Student by User ID (for login) ----------
    public Student getStudentByUserId(int userId) {
        String query = "SELECT s.*, d.name AS degree_name FROM students s " +
                "LEFT JOIN degrees d ON s.degree_id = d.id " +
                "WHERE s.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("student_reg_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("mobile_number"),
                        rs.getInt("degree_id"),
                        rs.getString("degree_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- Get Timetable for a Student (courses they are enrolled in)
    // ----------
    public List<Object[]> getTimetableByStudentId(int studentId) {
        List<Object[]> timetable = new ArrayList<>();
        String query = "SELECT t.day_of_week, t.time_slot, c.course_name " +
                "FROM timetables t " +
                "JOIN courses c ON t.course_id = c.id " +
                "JOIN enrollments e ON c.id = e.course_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY FIELD(t.day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'), t.time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                timetable.add(new Object[] {
                        rs.getString("day_of_week"),
                        rs.getString("time_slot"),
                        rs.getString("course_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timetable;
    }

    // ---------- Get Enrolled Courses with Grades ----------
    public List<Object[]> getEnrolledCoursesWithGrades(int studentId) {
        List<Object[]> courses = new ArrayList<>();
        String query = "SELECT c.course_code, c.course_name, c.credits, e.grade " +
                "FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.id " +
                "WHERE e.student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(new Object[] {
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getString("grade") != null ? rs.getString("grade") : "N/A"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }
}