package com.faculty.dao;

import com.faculty.model.Lecturer;
import com.faculty.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LecturerDAO {

    // ---------- READ: Get all lecturers (with department names) ----------
    public List<Lecturer> getAllLecturers() {
        List<Lecturer> lecturers = new ArrayList<>();
        String query = "SELECT l.*, d.name AS department_name FROM lecturers l " +
                "LEFT JOIN departments d ON l.department_id = d.id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lecturers.add(new Lecturer(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("mobile_number"),
                        rs.getInt("department_id"),
                        rs.getString("department_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lecturers;
    }

    // ---------- Helper: Get Department options for dropdown ----------
    public Map<String, Integer> getDepartmentOptions() {
        Map<String, Integer> departments = new HashMap<>();
        String query = "SELECT id, name FROM departments";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.put(rs.getString("name"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    // ---------- CREATE: Add a new lecturer (with user account) ----------
    public boolean addLecturer(Lecturer lecturer, String username, String password) {
        String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'Lecturer')";
        String insertLecturer = "INSERT INTO lecturers (user_id, full_name, email, mobile_number, department_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement lecturerStmt = conn.prepareStatement(insertLecturer)) {

                // 1. Create User account
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();

                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // 2. Create Lecturer profile
                    lecturerStmt.setInt(1, userId);
                    lecturerStmt.setString(2, lecturer.getFullName());
                    lecturerStmt.setString(3, lecturer.getEmail());
                    lecturerStmt.setString(4, lecturer.getMobileNumber());
                    lecturerStmt.setInt(5, lecturer.getDepartmentId());
                    lecturerStmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- UPDATE: Edit an existing lecturer ----------
    public boolean updateLecturer(Lecturer lecturer) {
        String query = "UPDATE lecturers SET full_name=?, email=?, mobile_number=?, department_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, lecturer.getFullName());
            stmt.setString(2, lecturer.getEmail());
            stmt.setString(3, lecturer.getMobileNumber());
            stmt.setInt(4, lecturer.getDepartmentId());
            stmt.setInt(5, lecturer.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- DELETE: Remove lecturer (cascade deletes user account) ----------
    public boolean deleteLecturer(int lecturerId) {
        // Delete the user record – ON DELETE CASCADE will remove the lecturer
        String query = "DELETE users FROM users " +
                "JOIN lecturers ON users.id = lecturers.user_id " +
                "WHERE lecturers.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, lecturerId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- Helper: Find lecturer by ID (for edit/delete) ----------
    public Lecturer getLecturerById(int id) {
        String query = "SELECT l.*, d.name AS department_name FROM lecturers l " +
                "LEFT JOIN departments d ON l.department_id = d.id " +
                "WHERE l.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Lecturer(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("mobile_number"),
                        rs.getInt("department_id"),
                        rs.getString("department_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ---------- Helper: Get lecturer by User ID (for login) ----------
    public Lecturer getLecturerByUserId(int userId) {
        String query = "SELECT l.*, d.name AS department_name FROM lecturers l " +
                "LEFT JOIN departments d ON l.department_id = d.id " +
                "WHERE l.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Lecturer(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("mobile_number"),
                        rs.getInt("department_id"),
                        rs.getString("department_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}