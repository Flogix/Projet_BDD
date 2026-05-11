package com.project.artconnect.dao;

import com.project.artconnect.model.Artist;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Artist entity.
 */
public class ArtistDao {

    private static final String URL = "jdbc:mysql://localhost:3306/artconnect?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Artist> findAll() {
        List<Artist> list = new ArrayList<>();
        String sql = "SELECT * FROM Artiste";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Artist(
                    rs.getString("nom"),
                    "", // bio non présente en DB de base
                    2000, // birthYear
                    rs.getString("email"), // adaptation si dispo
                    "Paris" // city
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void save(Artist artist) {
        String sql = "INSERT INTO Artiste (nom) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artist.getName());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Artist artist) {
        String sql = "UPDATE Artiste SET nom = ? WHERE nom = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artist.getName());
            ps.setString(2, artist.getName());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(String artistName) {
        String sql = "DELETE FROM Artiste WHERE nom = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Artist> findByCity(String city) {
        List<Artist> list = new ArrayList<>();
        String sql = "SELECT * FROM Artiste WHERE ville = ?"; // à adapter selon vos colonnes
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Artist(rs.getString("nom"), "", 2000, "", city));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}