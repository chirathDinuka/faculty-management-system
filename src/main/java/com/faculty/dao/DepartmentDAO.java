package com.faculty.dao;

import com.faculty.model.Department;
import com.faculty.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    // ---------- READ: Get all departments ----------
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String query = "SELECT * FROM departments ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                departments.add(new Department(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hod_name"),
                        rs.getInt("staff_count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    // ---------- CREATE: Add a new department ----------
    public boolean addDepartment(Department department) {
        String query = "INSERT INTO departments (name, hod_name, staff_count) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, department.getName());
            stmt.setString(2, department.getHodName());
            stmt.setInt(3, department.getStaffCount());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- UPDATE: Edit an existing department ----------
    public boolean updateDepartment(Department department) {
        String query = "UPDATE departments SET name=?, hod_name=?, staff_count=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, department.getName());
            stmt.setString(2, department.getHodName());
            stmt.setInt(3, department.getStaffCount());
            stmt.setInt(4, department.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- DELETE: Remove a department ----------
    public boolean deleteDepartment(int id) {
        String query = "DELETE FROM departments WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ---------- Helper: Get department by ID ----------
    public Department getDepartmentById(int id) {
        String query = "SELECT * FROM departments WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Department(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hod_name"),
                        rs.getInt("staff_count")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}