package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT id_galerie, nom, adresse, note FROM galerie WHERE id_galerie = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] findById() : " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> galleries = new ArrayList<>();
        String sql = "SELECT id_galerie, nom, adresse, note FROM galerie";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                galleries.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] findAll() : " + e.getMessage());
        }
        return galleries;
    }

    @Override
    public void save(Gallery gallery) {
        // Updated to handle id_organisateur which is required in the real schema
        String sql = "INSERT INTO galerie (nom, adresse, note, id_organisateur) VALUES (?, ?, ?, (SELECT id_organisateur FROM organisateur LIMIT 1))";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, gallery.getName());
            stmt.setString(2, gallery.getAddress());
            stmt.setDouble(3, gallery.getRating());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Gallery gallery) {
        String sql = "UPDATE galerie SET adresse = ?, note = ? WHERE nom = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, gallery.getAddress());
            stmt.setDouble(2, gallery.getRating());
            stmt.setString(3, gallery.getName());
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String name) {
        String sql = "DELETE FROM galerie WHERE nom = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcGalleryDao] delete() : " + e.getMessage());
        }
    }

    private Gallery mapRow(ResultSet rs) throws SQLException {
        Gallery gallery = new Gallery();
        gallery.setName(rs.getString("nom"));
        gallery.setAddress(rs.getString("adresse"));
        gallery.setRating(rs.getDouble("note"));
        return gallery;
    }
}
