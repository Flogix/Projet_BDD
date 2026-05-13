package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JDBC de ArtworkDao.
 *
 * Table cible : oeuvre (id_oeuvre, titre, type, prix, statut, id_artiste)
 * Join avec   : artiste (id_artiste, nom, prenom)
 *
 * Mapping statut BD ↔ Java :
 *   "Disponible" → Status.FOR_SALE
 *   "Vendu"      → Status.SOLD
 *   "Réservé"    → Status.EXHIBITED
 */
public class JdbcArtworkDao implements ArtworkDao {

    // ── Lecture ──────────────────────────────────────────────────────────────

    @Override
    public List<Artwork> findAll() {
        List<Artwork> artworks = new ArrayList<>();
        String sql = """
                SELECT o.titre, o.type, o.prix, o.statut,
                       a.nom, a.prenom
                FROM oeuvre o
                JOIN artiste a ON o.id_artiste = a.id_artiste
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                artworks.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] findAll() : " + e.getMessage());
        }
        return artworks;
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> artworks = new ArrayList<>();
        String[] parts = splitName(artistName);

        String sql = """
                SELECT o.titre, o.type, o.prix, o.statut,
                       a.nom, a.prenom
                FROM oeuvre o
                JOIN artiste a ON o.id_artiste = a.id_artiste
                WHERE a.nom = ? AND (a.prenom = ? OR ? IS NULL OR a.prenom IS NULL)
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, parts[0]);
            stmt.setString(2, parts[1].isBlank() ? null : parts[1]);
            stmt.setString(3, parts[1].isBlank() ? null : parts[1]);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) artworks.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] findByArtistName() : " + e.getMessage());
        }
        return artworks;
    }

    // ── Écriture ─────────────────────────────────────────────────────────────

    @Override
    public void save(Artwork artwork) {
        // Sous-requête pour récupérer id_artiste depuis le nom de l'artiste
        String[] parts = splitName(artwork.getArtist() != null ? artwork.getArtist().getName() : "");
        String statut = toDbStatut(artwork.getStatus());

        String sql = """
                INSERT INTO oeuvre (titre, type, prix, statut, id_artiste)
                VALUES (?, ?, ?, ?,
                    (SELECT id_artiste FROM artiste
                     WHERE nom = ? AND (prenom = ? OR prenom IS NULL) LIMIT 1))
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artwork.getTitle());
            stmt.setString(2, artwork.getType());
            stmt.setDouble(3, artwork.getPrice());
            stmt.setString(4, statut);
            stmt.setString(5, parts[0]);
            stmt.setString(6, parts[1].isBlank() ? null : parts[1]);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] save() : " + e.getMessage());
        }
    }

    @Override
    public void update(Artwork artwork) {
        String statut = toDbStatut(artwork.getStatus());
        String sql = "UPDATE oeuvre SET type = ?, prix = ?, statut = ? WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artwork.getType());
            stmt.setDouble(2, artwork.getPrice());
            stmt.setString(3, statut);
            stmt.setString(4, artwork.getTitle());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] update() : " + e.getMessage());
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM oeuvre WHERE titre = ?";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[JdbcArtworkDao] delete() : " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Artwork mapRow(ResultSet rs) throws SQLException {
        Artist artist = new Artist();
        String nom    = rs.getString("nom");
        String prenom = rs.getString("prenom");
        artist.setName(nom + (prenom != null && !prenom.isBlank() ? " " + prenom : ""));

        Artwork artwork = new Artwork();
        artwork.setTitle(rs.getString("titre"));
        artwork.setType(rs.getString("type"));
        artwork.setPrice(rs.getDouble("prix"));
        artwork.setArtist(artist);
        artwork.setStatus(fromDbStatut(rs.getString("statut")));
        return artwork;
    }

    private String toDbStatut(Artwork.Status status) {
        if (status == null) return "Disponible";
        return switch (status) {
            case SOLD     -> "Vendu";
            case EXHIBITED -> "Réservé";
            default       -> "Disponible";
        };
    }

    private Artwork.Status fromDbStatut(String statut) {
        if (statut == null) return Artwork.Status.FOR_SALE;
        return switch (statut) {
            case "Vendu"   -> Artwork.Status.SOLD;
            case "Réservé" -> Artwork.Status.EXHIBITED;
            default        -> Artwork.Status.FOR_SALE;
        };
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"", ""};
        int idx = fullName.indexOf(' ');
        if (idx == -1) return new String[]{fullName, ""};
        return new String[]{fullName.substring(0, idx), fullName.substring(idx + 1)};
    }
}