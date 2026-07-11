package com.faculty.dao;

import com.faculty.model.Degree;
import com.faculty.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DegreeDAO {

    // ---------- READ: Get all degrees (with department names) ----------
    public List<Degree> getAllDegrees() {
        List<Degree> degrees = new ArrayList<>();
        String query = "SELECT d.*, dept.name AS department_name FROM degrees d " +
                "LEFT JOIN departments dept ON d.department_id = dept.id " +
                "ORDER BY d.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                degrees.add(new Degree(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("department_id"),
                        rs.getString("department_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return degrees;
    }

    // ---------- Helper: Get Department options for dropdown ----------
    public Map<String, Integer> getDepartmentOptions() {
        Map<String, Integer> departments = new HashMap<>();
        String query = "SELECT id, name FROM departments ORDER BY name";

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

    // ---------- CREATE: Add a new degree ----------
    public boolean addDegree(Degree degree) {
        String query = "INSERT INTO degrees (name, department_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, degree.getName());
            stmt.setInt(2, degree.getDepartmentId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- UPDATE: Edit an existing degree ----------
    public boolean updateDegree(Degree degree) {
        String query = "UPDATE degrees SET name=?, department_id=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, degree.getName());
            stmt.setInt(2, degree.getDepartmentId());
            stmt.setInt(3, degree.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- DELETE: Remove a degree ----------
    public boolean deleteDegree(int id) {
        String query = "DELETE FROM degrees WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- Helper: Get degree by ID ----------
    public Degree getDegreeById(int id) {
        String query = "SELECT d.*, dept.name AS department_name FROM degrees d " +
                "LEFT JOIN departments dept ON d.department_id = dept.id " +
                "WHERE d.id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Degree(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("department_id"),
                        rs.getString("department_name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}