package com.project.artconnect.ui;

import com.project.artconnect.util.ConnectionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class AnalyticsController {

    @FXML private Label labelNbArtists;
    @FXML private Label labelNbArtworks;
    @FXML private Label labelAvgPrice;
    @FXML private Label labelTotalSales;
    @FXML private Label labelMostExpensive;

    @FXML private TableView<String[]>             cityTable;
    @FXML private TableColumn<String[], String>   cityNameCol;
    @FXML private TableColumn<String[], String>   cityCountCol;

    @FXML private TableView<String[]>             topArtistTable;
    @FXML private TableColumn<String[], String>   topArtistNameCol;
    @FXML private TableColumn<String[], String>   topArtistCountCol;
    @FXML private TableColumn<String[], String>   topArtistValueCol;

    @FXML private TableView<String[]>             galleryRankTable;
    @FXML private TableColumn<String[], String>   galleryNameCol;
    @FXML private TableColumn<String[], String>   galleryNoteCol;
    @FXML private TableColumn<String[], String>   galleryExpoCol;

    @FXML private TableView<String[]>             eventsTable;
    @FXML private TableColumn<String[], String>   eventTypeCol;
    @FXML private TableColumn<String[], String>   eventTitleCol;
    @FXML private TableColumn<String[], String>   eventDateCol;

    @FXML
    public void initialize() {
        setupColumns();
        loadAllStats();
    }

    private void setupColumns() {
        cityNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        cityCountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        topArtistNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        topArtistCountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        topArtistValueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        galleryNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        galleryNoteCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        galleryExpoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

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
            System.out.println("[AnalyticsController] Statistiques chargées.");
        } catch (SQLException e) {
            System.err.println("[AnalyticsController] Erreur SQL : " + e.getMessage());
        }
    }

    private void loadKpis(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM artiste")) {
            if (rs.next()) labelNbArtists.setText(String.valueOf(rs.getInt(1)));
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM oeuvre")) {
            if (rs.next()) labelNbArtworks.setText(String.valueOf(rs.getLong(1)));
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(prix) FROM oeuvre")) {
            if (rs.next()) labelAvgPrice.setText(String.format("%.2f €", rs.getDouble(1)));
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(prix) FROM oeuvre WHERE UPPER(TRIM(statut)) = 'VENDU'")) {
            if (rs.next()) labelTotalSales.setText(String.format("%.2f €", rs.getDouble(1)));
        }

        String sqlMax = "SELECT titre, prix FROM oeuvre WHERE prix = (SELECT MAX(prix) FROM oeuvre) LIMIT 1";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlMax)) {
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
        String sql = "SELECT CONCAT(a.nom, ' ', IFNULL(a.prenom,'')), count_art, val_port " +
                     "FROM artiste a JOIN (" +
                     "  SELECT id_artiste, COUNT(*) as count_art, SUM(prix) as val_port " +
                     "  FROM oeuvre GROUP BY id_artiste ORDER BY count_art DESC LIMIT 5" +
                     ") o ON a.id_artiste = o.id_artiste";
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
        String sql = "SELECT g.nom, g.note, COUNT(e.id_exposition) " +
                     "FROM galerie g LEFT JOIN exposition e ON g.id_galerie = e.id_galerie " +
                     "GROUP BY g.id_galerie ORDER BY g.note DESC";
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
        String sql = "SELECT 'Exposition', titre, date_debut FROM exposition " +
                     "UNION ALL " +
                     "SELECT 'Atelier', titre, DATE(date_heure) FROM atelier " +
                     "ORDER BY 3 DESC LIMIT 10";
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
