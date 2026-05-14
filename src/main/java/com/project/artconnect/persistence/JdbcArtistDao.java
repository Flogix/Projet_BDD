package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de ArtistDao.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        List<Artist> artists = new ArrayList<>();
        // Note: bio, phone, website, social_media are missing from the current DB schema
        String sql = "SELECT nom, prenom, annee_naissance, email, ville, discipline FROM artiste";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                artists.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] findAll() : " + e.getMessage());
        }
        return artists;
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT nom, prenom, annee_naissance, email, ville, discipline FROM artiste WHERE ville = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    artists.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] findByCity() : " + e.getMessage());
        }
        return artists;
    }

    @Override
    public void save(Artist artist) {
        String[] parts = splitName(artist.getName());
        String discipline = artist.getDisciplines().isEmpty()
                ? null : artist.getDisciplines().get(0).getName();

        String sql = "INSERT INTO artiste (nom, prenom, annee_naissance, email, ville, discipline) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.setObject(3, artist.getBirthYear());
            stmt.setString(4, artist.getContactEmail());
            stmt.setString(5, artist.getCity());
            stmt.setString(6, discipline);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Artist artist) {
        String[] parts = splitName(artist.getName());
        String discipline = artist.getDisciplines().isEmpty()
                ? null : artist.getDisciplines().get(0).getName();

        String sql = "UPDATE artiste SET nom = ?, prenom = ?, ville = ?, discipline = ?, annee_naissance = ? WHERE email = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(3, artist.getCity());
            stmt.setString(4, discipline);
            stmt.setObject(5, artist.getBirthYear());
            stmt.setString(6, artist.getContactEmail());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String artistName) {
        String[] parts = splitName(artistName);
        String sql = "DELETE FROM artiste WHERE nom = ? AND (prenom = ? OR prenom IS NULL)";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtistDao] delete() : " + e.getMessage());
        }
    }

    private Artist mapRow(ResultSet rs) throws SQLException {
        Artist artist = new Artist();

        String nom    = rs.getString("nom");
        String prenom = rs.getString("prenom");
        artist.setName(nom + (prenom != null && !prenom.isBlank() ? " " + prenom : ""));
        artist.setCity(rs.getString("ville"));
        artist.setContactEmail(rs.getString("email"));

        int annee = rs.getInt("annee_naissance");
        if (!rs.wasNull()) artist.setBirthYear(annee);

        String discipline = rs.getString("discipline");
        if (discipline != null && !discipline.isBlank()) {
            artist.getDisciplines().add(new Discipline(discipline));
        }
        return artist;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"", ""};
        int idx = fullName.indexOf(' ');
        if (idx == -1) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, idx), fullName.substring(idx + 1)};
    }
}
