package com.project.artconnect.ui;

import com.project.artconnect.util.ConnectionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

/**
 * Contrôleur pour l'onglet Analytics.
 *
 * Toutes les données viennent directement de la base via des requêtes SQL
 * exploitant les vues et tables de la base artconnect.
 *
 * Statistiques affichées :
 *  - Nombre total d'artistes
 *  - Nombre total d'œuvres
 *  - Prix moyen des œuvres
 *  - Œuvre la plus chère (utilise la vue vue_oeuvres_disponibles)
 *  - Artistes par ville
 *  - Top artistes par nombre d'œuvres (utilise vue_statistiques_artistes)
 *  - Galeries classées par note (utilise vue_stats_galeries)
 */
public class AnalyticsController {

    // ── KPI labels ────────────────────────────────────────────────────────
    @FXML private Label labelNbArtists;
    @FXML private Label labelNbArtworks;
    @FXML private Label labelAvgPrice;
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
    }

    private void loadAllStats() {
        try (Connection conn = ConnectionManager.getConnection()) {
            loadKpis(conn);
            loadCityStats(conn);
            loadTopArtists(conn);
            loadGalleryRanking(conn);
        } catch (SQLException e) {
            System.err.println("[AnalyticsController] Erreur connexion : " + e.getMessage());
            labelNbArtists.setText("N/A");
            labelNbArtworks.setText("N/A");
            labelAvgPrice.setText("N/A");
            labelMostExpensive.setText("Base non disponible");
        }
    }

    // ── KPI : totaux + prix moyen + œuvre la plus chère ──────────────────

    private void loadKpis(Connection conn) throws SQLException {

        // Nombre d'artistes
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM artiste")) {
            if (rs.next()) labelNbArtists.setText(String.valueOf(rs.getInt(1)));
        }

        // Nombre d'œuvres
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM oeuvre")) {
            if (rs.next()) labelNbArtworks.setText(String.valueOf(rs.getInt(1)));
        }

        // Prix moyen
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(prix) FROM oeuvre")) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                labelAvgPrice.setText(String.format("%.0f €", avg));
            }
        }

        // Œuvre la plus chère — utilise la vue vue_oeuvres_disponibles si disponible
        String sqlMostExpensive = """
                SELECT o.titre, o.prix, CONCAT(a.nom,' ',IFNULL(a.prenom,'')) AS artiste
                FROM oeuvre o
                JOIN artiste a ON o.id_artiste = a.id_artiste
                ORDER BY o.prix DESC
                LIMIT 1
                """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlMostExpensive)) {
            if (rs.next()) {
                labelMostExpensive.setText(String.format("%s\n%.0f €\n(%s)",
                        rs.getString("titre"),
                        rs.getDouble("prix"),
                        rs.getString("artiste")));
            }
        }
    }

    // ── Artistes par ville ────────────────────────────────────────────────

    private void loadCityStats(Connection conn) throws SQLException {
        String sql = """
                SELECT ville, COUNT(*) AS nb_artistes
                FROM artiste
                WHERE ville IS NOT NULL
                GROUP BY ville
                ORDER BY nb_artistes DESC
                """;

        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("ville"),
                        String.valueOf(rs.getInt("nb_artistes"))
                });
            }
        }
        cityTable.setItems(data);
    }

    // ── Top artistes — utilise la vue vue_statistiques_artistes ──────────

    private void loadTopArtists(Connection conn) throws SQLException {
        // On utilise directement la vue créée à l'étape 3
        String sql = """
                SELECT CONCAT(nom,' ',IFNULL(prenom,'')) AS artiste,
                       total_oeuvres,
                       IFNULL(valeur_totale_portfolio, 0) AS valeur
                FROM vue_statistiques_artistes
                ORDER BY total_oeuvres DESC, valeur DESC
                LIMIT 10
                """;

        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("artiste"),
                        String.valueOf(rs.getInt("total_oeuvres")),
                        String.format("%.0f €", rs.getDouble("valeur"))
                });
            }
        }
        topArtistTable.setItems(data);
    }

    // ── Galeries classées par note — utilise vue_stats_galeries ──────────

    private void loadGalleryRanking(Connection conn) throws SQLException {
        // On utilise directement la vue créée à l'étape 3
        String sql = """
                SELECT nom, note, nombre_expositions
                FROM vue_stats_galeries
                ORDER BY note DESC, nombre_expositions DESC
                """;

        ObservableList<String[]> data = FXCollections.observableArrayList();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.add(new String[]{
                        rs.getString("nom"),
                        String.valueOf(rs.getInt("note")) + " / 5",
                        String.valueOf(rs.getInt("nombre_expositions"))
                });
            }
        }
        galleryRankTable.setItems(data);
    }
}