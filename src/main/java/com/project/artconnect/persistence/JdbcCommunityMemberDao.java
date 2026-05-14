package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> members = new ArrayList<>();
        String sql = "SELECT nom, prenom, email, ville FROM visiteur";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] findAll() : " + e.getMessage());
        }
        return members;
    }

    @Override
    public void save(CommunityMember member) {
        String sql = "INSERT INTO visiteur (nom, prenom, email, ville) VALUES (?, ?, ?, ?)";
        String[] parts = splitName(member.getName());

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getCity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(CommunityMember member) {
        String sql = "UPDATE visiteur SET nom = ?, prenom = ?, ville = ? WHERE email = ?";
        String[] parts = splitName(member.getName());

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt =prepareStatement(conn, sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(3, member.getCity());
            stmt.setString(4, member.getEmail());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String email) {
        String sql = "DELETE FROM visiteur WHERE email = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] delete() : " + e.getMessage());
        }
    }

    @Override
    public List<CommunityMember> findByEmail(String email) {
        List<CommunityMember> members = new ArrayList<>();
        String sql = "SELECT nom, prenom, email, ville FROM visiteur WHERE email = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcCommunityMemberDao] findByEmail() : " + e.getMessage());
        }
        return members;
    }

    private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }

    private CommunityMember mapRow(ResultSet rs) throws SQLException {
        CommunityMember member = new CommunityMember();
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        member.setName(nom + (prenom != null && !prenom.isBlank() ? " " + prenom : ""));
        member.setEmail(rs.getString("email"));
        member.setCity(rs.getString("ville"));
        return member;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"", ""};
        int idx = fullName.indexOf(' ');
        if (idx == -1) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, idx), fullName.substring(idx + 1)};
    }
}
