package com.faculty.dao;

import com.faculty.model.Course;
import com.faculty.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseDAO {

    // ---------- READ: Get all courses (with lecturer names) ----------
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT c.*, l.full_name AS lecturer_name FROM courses c " +
                "LEFT JOIN lecturers l ON c.lecturer_id = l.id " +
                "ORDER BY c.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getInt("lecturer_id"),
                        rs.getString("lecturer_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    // ---------- Helper: Get Lecturer options for dropdown ----------
    public Map<String, Integer> getLecturerOptions() {
        Map<String, Integer> lecturers = new HashMap<>();
        String query = "SELECT id, full_name FROM lecturers ORDER BY full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lecturers.put(rs.getString("full_name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lecturers;
    }

    // ---------- CREATE: Add a new course ----------
    public boolean addCourse(Course course) {
        String query = "INSERT INTO courses (course_code, course_name, credits, lecturer_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getLecturerId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- UPDATE: Edit an existing course ----------
    public boolean updateCourse(Course course) {
        String query = "UPDATE courses SET course_code=?, course_name=?, credits=?, lecturer_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, course.getCourseCode());
            stmt.setString(2, course.getCourseName());
            stmt.setInt(3, course.getCredits());
            stmt.setInt(4, course.getLecturerId());
            stmt.setInt(5, course.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- DELETE: Remove a course ----------
    public boolean deleteCourse(int id) {
        String query = "DELETE FROM courses WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- Helper: Get course by ID ----------
    public Course getCourseById(int id) {
        String query = "SELECT c.*, l.full_name AS lecturer_name FROM courses c " +
                "LEFT JOIN lecturers l ON c.lecturer_id = l.id " +
                "WHERE c.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Course(
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                        rs.getInt("lecturer_id"),
                        rs.getString("lecturer_name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}