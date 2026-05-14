package com.project.artconnect.ui;

import com.project.artconnect.util.ConnectionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

/**
 * Contrôleur pour l'onglet Analytics.
 */
public class AnalyticsController {

    // ── KPI labels ────────────────────────────────────────────────────────
    @FXML private Label labelNbArtists;
    @FXML private Label labelNbArtworks;
    @FXML private Label labelAvgPrice;
    @FXML private Label labelTotalSales;
    @FXML private Label labelMostExpensive;

    // ── Tableau : artistes par ville ──────────────────────────────────────
    @FXML private TableView<String[]>             cityTable;
    @FXML private TableColumn<String[], String>   cityNameCol;
    @FXML private TableColumn<String[], String>   cityCountCol;

    // ── Tableau : top artistes ────────────────────────────────────────────
    @FXML private TableView<String[]>             topArtistTable;
    @FXML private TableColumn<String[], String>   topArtistNameCol;
    @FXML private TableColumn<String[], String>   topArtistCountCol;
    @FXML private TableColumn<String[], String>   topArtistValueCol;

    // ── Tableau : galeries ────────────────────────────────────────────────
    @FXML private TableView<String[]>             galleryRankTable;
    @FXML private TableColumn<String[], String>   galleryNameCol;
    @FXML private TableColumn<String[], String>   galleryNoteCol;
    @FXML private TableColumn<String[], String>   galleryExpoCol;

    // ── Tableau : événements ──────────────────────────────────────────────
    @FXML private TableView<String[]>             eventsTable;
    @FXML private TableColumn<String[], String>   eventTypeCol;
    @FXML private TableColumn<String[], String>   eventTitleCol;
    @FXML private TableColumn<String[], String>   eventDateCol;

    // ─────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupColumns();
        loadAllStats();
    }

    private void setupColumns() {
        // Artistes par ville
        cityNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        cityCountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        // Top artistes
        topArtistNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        topArtistCountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        topArtistValueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        // Galeries
        galleryNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        galleryNoteCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        galleryExpoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        // Evénements
        eventTypeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        eventTitleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        eventDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
    }

    private void loadAllStats() {
        System.out.println("[AnalyticsController] Chargement des statistiques...");
        try (Connection conn = ConnectionManager.getConnection()) {
            loadKpis(conn);
            loadCityStats(conn);
            loadTopArtists(conn);
            loadGalleryRanking(conn);
            loadUpcomingEvents(conn);
            System.out.println("[AnalyticsController] Statistiques chargées avec succès.");
        } catch (SQLException e) {
            System.err.println("[AnalyticsController] Erreur : " + e.getMessage());
        }
    }

    private void loadKpis(Connection conn) throws SQLException {
        // Nb Artistes
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM artiste")) {
            if (rs.next()) {
                int count = rs.getInt(1);
                labelNbArtists.setText(String.valueOf(count));
                System.out.println("[AnalyticsController] Artistes : " + count);
            }
        }

        // Nb Oeuvres
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM oeuvre")) {
            if (rs.next()) labelNbArtworks.setText(String.valueOf(rs.getInt(1)));
        }

        // Prix Moyen
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(prix) FROM oeuvre")) {
            if (rs.next()) labelAvgPrice.setText(String.format("%.0f €", rs.getDouble(1)));
        }

        // Ventes
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(prix) FROM oeuvre WHERE statut = 'Vendu'")) {
            if (rs.next()) labelTotalSales.setText(String.format("%.0f €", rs.getDouble(1)));
        }

        // Plus chère
        String sql = "SELECT o.titre, o.prix FROM oeuvre o ORDER BY o.prix DESC LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) labelMostExpensive.setText(rs.getString("titre") + " (" + rs.getDouble("prix") + " €)");
        }
    }

    private void loadCityStats(Connection conn) throws SQLException {
        String sql = "SELECT ville, COUNT(*) FROM artiste GROUP BY ville ORDER BY 2 DESC";
        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        }
        cityTable.setItems(data);
    }

    private void loadTopArtists(Connection conn) throws SQLException {
        String sql = """
                SELECT CONCAT(a.nom, ' ', a.prenom), COUNT(o.id_oeuvre), SUM(o.prix)
                FROM artiste a
                JOIN oeuvre o ON a.id_artiste = o.id_artiste
                GROUP BY a.id_artiste
                ORDER BY 2 DESC LIMIT 5
                """;
        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3) + " €"});
            }
        }
        topArtistTable.setItems(data);
    }

    private void loadGalleryRanking(Connection conn) throws SQLException {
        String sql = """
                SELECT g.nom, g.note, COUNT(e.id_exposition)
                FROM galerie g
                LEFT JOIN exposition e ON g.id_galerie = e.id_galerie
                GROUP BY g.id_galerie
                ORDER BY g.note DESC
                """;
        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString(1), rs.getString(2) + "/5", rs.getString(3)});
            }
        }
        galleryRankTable.setItems(data);
    }

    private void loadUpcomingEvents(Connection conn) throws SQLException {
        String sql = """
                SELECT 'Exposition', titre, date_debut FROM exposition
                UNION ALL
                SELECT 'Atelier', titre, DATE(date_heure) FROM atelier
                ORDER BY 3 DESC LIMIT 10
                """;
        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3)});
            }
        }
        eventsTable.setItems(data);
    }
}
