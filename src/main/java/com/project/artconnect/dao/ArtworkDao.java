package com.project.artconnect.dao;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ArtworkDao {

    private static final String URL = "jdbc:mysql://localhost:3306/artconnect?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Artwork> findAll() {
        List<Artwork> list = new ArrayList<>();
        String sql = "SELECT * FROM vue_details_oeuvres"; // On utilise votre vue étape 3
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artist artist = new Artist();
                artist.setName(rs.getString("nom_artiste"));
                list.add(new Artwork(
                    rs.getString("titre_oeuvre"),
                    2024,
                    rs.getString("type"),
                    rs.getDouble("prix"),
                    artist
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(Artwork artwork) {
        String sql = "INSERT INTO Oeuvre (titre, prix) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artwork.getTitle());
            ps.setDouble(2, artwork.getPrice());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Artwork artwork) {
        String sql = "UPDATE Oeuvre SET prix = ? WHERE titre = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, artwork.getPrice());
            ps.setString(2, artwork.getTitle());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(String title) {
        String sql = "DELETE FROM Oeuvre WHERE titre = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> list = new ArrayList<>();
        String sql = "SELECT * FROM vue_details_oeuvres WHERE nom_artiste = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artist artist = new Artist();
                    artist.setName(artistName);
                    list.add(new Artwork(rs.getString("titre_oeuvre"), 2024, rs.getString("type"), rs.getDouble("prix"), artist));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}