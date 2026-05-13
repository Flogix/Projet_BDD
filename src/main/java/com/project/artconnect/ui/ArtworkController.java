package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Contrôleur pour l'onglet Artworks.
 * Gère : affichage, ajout, modification, suppression (CRUD persistant en base).
 */
public class ArtworkController {

    // ── Tableau ───────────────────────────────────────────────────────────
    @FXML private TableView<Artwork>           artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;

    // ── Formulaire CRUD ───────────────────────────────────────────────────
    @FXML private TextField            fieldTitle;
    @FXML private TextField            fieldArtistName;
    @FXML private TextField            fieldType;
    @FXML private TextField            fieldPrice;
    @FXML private ComboBox<String>     fieldStatus;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();

    // ─────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null
                        ? cellData.getValue().getArtist().getName() : "Unknown"));

        fieldStatus.setItems(FXCollections.observableArrayList("FOR_SALE", "SOLD", "EXHIBITED"));

        // Clic sur une ligne → remplit le formulaire
        artworkTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> {
                    if (selected != null) fillForm(selected);
                });

        refreshTable();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        Artwork artwork = buildArtworkFromForm();
        if (artwork == null) return;

        artworkService.createArtwork(artwork);
        refreshTable();
        clearForm();
        showInfo("Œuvre ajoutée", artwork.getTitle() + " a été ajoutée en base.");
    }

    @FXML
    private void handleUpdate() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Cliquez d'abord sur une œuvre dans le tableau.");
            return;
        }
        Artwork updated = buildArtworkFromForm();
        if (updated == null) return;

        artworkService.updateArtwork(updated);
        refreshTable();
        clearForm();
        showInfo("Œuvre modifiée", updated.getTitle() + " a été mis à jour.");
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Cliquez d'abord sur une œuvre dans le tableau.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer \"" + selected.getTitle() + "\" ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
                clearForm();
                showInfo("Supprimée", "\"" + selected.getTitle() + "\" a été supprimée.");
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private Artwork buildArtworkFromForm() {
        String title = fieldTitle.getText().trim();
        String artistName = fieldArtistName.getText().trim();
        if (title.isEmpty()) {
            showWarning("Champ requis", "Le titre est obligatoire.");
            return null;
        }
        double price = 0.0;
        try {
            if (!fieldPrice.getText().isBlank())
                price = Double.parseDouble(fieldPrice.getText().trim());
        } catch (NumberFormatException e) {
            showWarning("Format invalide", "Le prix doit être un nombre.");
            return null;
        }

        Artist artist = new Artist();
        artist.setName(artistName.isEmpty() ? "Inconnu" : artistName);

        Artwork artwork = new Artwork(title, null, fieldType.getText().trim(), price, artist);

        String statusStr = fieldStatus.getValue();
        if (statusStr != null) {
            artwork.setStatus(switch (statusStr) {
                case "SOLD"     -> Artwork.Status.SOLD;
                case "EXHIBITED" -> Artwork.Status.EXHIBITED;
                default         -> Artwork.Status.FOR_SALE;
            });
        }
        return artwork;
    }

    private void fillForm(Artwork a) {
        fieldTitle.setText(a.getTitle());
        fieldArtistName.setText(a.getArtist() != null ? a.getArtist().getName() : "");
        fieldType.setText(a.getType() != null ? a.getType() : "");
        fieldPrice.setText(String.valueOf(a.getPrice()));
        if (a.getStatus() != null)
            fieldStatus.setValue(a.getStatus().name());
    }

    private void clearForm() {
        fieldTitle.clear(); fieldArtistName.clear(); fieldType.clear();
        fieldPrice.clear(); fieldStatus.setValue(null);
        artworkTable.getSelectionModel().clearSelection();
    }

    private void showInfo(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK)
        {{ setTitle(title); }}.showAndWait();
    }

    private void showWarning(String title, String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK)
        {{ setTitle(title); }}.showAndWait();
    }
}