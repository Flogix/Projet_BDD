package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = """
                SELECT id_atelier, titre_atelier, date_heure, prix, 
                       nom_instructeur, prenom_instructeur
                FROM vue_details_ateliers
                WHERE id_atelier = ?
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] findById() : " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> workshops = new ArrayList<>();
        // Query the table via the view
        String sql = """
                SELECT id_atelier, titre_atelier, date_heure, prix, 
                       nom_instructeur, prenom_instructeur
                FROM vue_details_ateliers
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Workshop w = mapRow(rs);
                workshops.add(w);
            }
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] findAll() : " + e.getMessage());
        }
        return workshops;
    }

    @Override
    public void save(Workshop workshop) {
        String[] parts = splitName(workshop.getInstructor() != null ? workshop.getInstructor().getName() : "");
        String sql = """
                INSERT INTO atelier (titre, date_heure, prix, id_artiste)
                VALUES (?, ?, ?, 
                    (SELECT id_artiste FROM artiste WHERE nom = ? AND (prenom = ? OR prenom IS NULL) LIMIT 1))
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, workshop.getTitle());
            stmt.setTimestamp(2, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
            stmt.setDouble(3, workshop.getPrice());
            stmt.setString(4, parts[0]);
            stmt.setString(5, parts[1].isBlank() ? null : parts[1]);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Workshop workshop) {
        String[] parts = splitName(workshop.getInstructor() != null ? workshop.getInstructor().getName() : "");
        String sql = """
                UPDATE atelier SET date_heure = ?, prix = ?,
                id_artiste = (SELECT id_artiste FROM artiste WHERE nom = ? AND (prenom = ? OR prenom IS NULL) LIMIT 1)
                WHERE titre = ?
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
            stmt.setDouble(2, workshop.getPrice());
            stmt.setString(3, parts[0]);
            stmt.setString(4, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(5, workshop.getTitle());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM atelier WHERE titre = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, title);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] delete() : " + e.getMessage());
        }
    }

    @Override
    public double calculateMaxRevenue(String title) {
        String sql = "SELECT (prix * places_max) FROM atelier WHERE titre = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] calculateMaxRevenue() : " + e.getMessage());
        }
        return 0.0;
    }

    private Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setTitle(rs.getString("titre_atelier"));
        
        Timestamp timestamp = rs.getTimestamp("date_heure");
        if (timestamp != null) {
            workshop.setDate(timestamp.toLocalDateTime());
        }
        
        workshop.setPrice(rs.getDouble("prix"));
        
        String instNom = rs.getString("nom_instructeur");
        if (instNom != null) {
            String instPrenom = rs.getString("prenom_instructeur");
            Artist artist = new Artist();
            artist.setName(instNom + (instPrenom != null && !instPrenom.isBlank() ? " " + instPrenom : ""));
            workshop.setInstructor(artist);
        }
        
        return workshop;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"", ""};
        int idx = fullName.indexOf(' ');
        if (idx == -1) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, idx), fullName.substring(idx + 1)};
    }
}
