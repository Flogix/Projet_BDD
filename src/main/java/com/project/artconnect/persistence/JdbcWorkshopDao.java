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
                SELECT w.id_atelier, w.titre, w.date_heure, w.prix, w.niveau,
                       a.nom, a.prenom
                FROM atelier w
                LEFT JOIN artiste a ON w.id_artiste = a.id_artiste
                WHERE w.id_atelier = ?
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
        String sql = """
                SELECT w.id_atelier, w.titre, w.date_heure, w.prix, w.niveau,
                       a.nom, a.prenom
                FROM atelier w
                LEFT JOIN artiste a ON w.id_artiste = a.id_artiste
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                workshops.add(mapRow(rs));
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
                INSERT INTO atelier (titre, date_heure, prix, niveau, id_artiste)
                VALUES (?, ?, ?, ?,
                    (SELECT id_artiste FROM artiste WHERE nom = ? AND (prenom = ? OR prenom IS NULL) LIMIT 1))
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, workshop.getTitle());
            stmt.setTimestamp(2, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
            stmt.setDouble(3, workshop.getPrice());
            stmt.setString(4, workshop.getLevel());
            stmt.setString(5, parts[0]);
            stmt.setString(6, parts[1].isBlank() ? null : parts[1]);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcWorkshopDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Workshop workshop) {
        String[] parts = splitName(workshop.getInstructor() != null ? workshop.getInstructor().getName() : "");
        String sql = """
                UPDATE atelier SET date_heure = ?, prix = ?, niveau = ?,
                id_artiste = (SELECT id_artiste FROM artiste WHERE nom = ? AND (prenom = ? OR prenom IS NULL) LIMIT 1)
                WHERE titre = ?
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
            stmt.setDouble(2, workshop.getPrice());
            stmt.setString(3, workshop.getLevel());
            stmt.setString(4, parts[0]);
            stmt.setString(5, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(6, workshop.getTitle());
            
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
        // Current DB schema uses prix and places_max
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
        workshop.setTitle(rs.getString("titre"));
        
        Timestamp timestamp = rs.getTimestamp("date_heure");
        if (timestamp != null) {
            workshop.setDate(timestamp.toLocalDateTime());
        }
        
        workshop.setPrice(rs.getDouble("prix"));
        workshop.setLevel(rs.getString("niveau"));
        
        String instNom = rs.getString("nom");
        if (instNom != null) {
            String instPrenom = rs.getString("prenom");
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
