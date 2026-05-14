package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> exhibitions = new ArrayList<>();
        // Note: view v_exposition_details is missing, using direct join
        String sql = """
                SELECT e.titre, e.date_debut, e.date_fin, e.theme, g.nom, g.adresse
                FROM exposition e
                LEFT JOIN galerie g ON e.id_galerie = g.id_galerie
                """;
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                exhibitions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] findAll() : " + e.getMessage());
        }
        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql = """
                INSERT INTO exposition (titre, date_debut, date_fin, theme, id_galerie)
                VALUES (?, ?, ?, ?,
                    (SELECT id_galerie FROM galerie WHERE nom = ? LIMIT 1))
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, exhibition.getTitle());
            stmt.setDate(2, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            stmt.setDate(3, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            stmt.setString(4, exhibition.getTheme());
            stmt.setString(5, exhibition.getGallery() != null ? exhibition.getGallery().getName() : "");
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = """
                UPDATE exposition SET date_debut = ?, date_fin = ?, theme = ?,
                id_galerie = (SELECT id_galerie FROM galerie WHERE nom = ? LIMIT 1)
                WHERE titre = ?
                """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
            stmt.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
            stmt.setString(3, exhibition.getTheme());
            stmt.setString(4, exhibition.getGallery() != null ? exhibition.getGallery().getName() : "");
            stmt.setString(5, exhibition.getTitle());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM exposition WHERE titre = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, title);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("[JdbcExhibitionDao] delete() : " + e.getMessage());
        }
    }

    private Exhibition mapRow(ResultSet rs) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(rs.getString("titre"));
        Date startDate = rs.getDate("date_debut");
        Date endDate = rs.getDate("date_fin");
        
        if (startDate != null) exhibition.setStartDate(startDate.toLocalDate());
        if (endDate != null) exhibition.setEndDate(endDate.toLocalDate());
        
        exhibition.setTheme(rs.getString("theme"));
        
        String galleryName = rs.getString("nom");
        if (galleryName != null) {
            Gallery gallery = new Gallery();
            gallery.setName(galleryName);
            gallery.setAddress(rs.getString("adresse"));
            exhibition.setGallery(gallery);
        }
        
        return exhibition;
    }
}
