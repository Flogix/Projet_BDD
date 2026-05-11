package com.project.artconnect.dao;

import com.project.artconnect.model.CommunityMember;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CommunityMember entity.
 */
public class CommunityMemberDao {

    private static final String URL = "jdbc:mysql://localhost:3306/artconnect?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<CommunityMember> findAll() {
        List<CommunityMember> list = new ArrayList<>();
        String sql = "SELECT nom, email FROM Visiteur";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CommunityMember(rs.getString("nom"), rs.getString("email")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(CommunityMember member) {
        String sql = "INSERT INTO Visiteur (nom, email) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(CommunityMember member) {
        String sql = "UPDATE Visiteur SET email = ? WHERE nom = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getEmail());
            ps.setString(2, member.getName());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(String email) {
        String sql = "DELETE FROM Visiteur WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<CommunityMember> findByEmail(String email) {
        List<CommunityMember> list = new ArrayList<>();
        String sql = "SELECT nom, email FROM Visiteur WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CommunityMember(rs.getString("nom"), rs.getString("email")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}