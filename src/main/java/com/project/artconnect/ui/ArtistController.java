package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Contrôleur pour l'onglet Artists.
 * Gère : recherche, ajout, modification, suppression (CRUD persistant en base).
 */
public class ArtistController {

    // ── Recherche ─────────────────────────────────────────────────────────
    @FXML private TextField            searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;

    // ── Tableau ───────────────────────────────────────────────────────────
    @FXML private TableView<Artist>          artistTable;
    @FXML private TableColumn<Artist, String>  nameColumn;
    @FXML private TableColumn<Artist, String>  cityColumn;
    @FXML private TableColumn<Artist, String>  emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;

    // ── Formulaire CRUD ───────────────────────────────────────────────────
    @FXML private TextField fieldName;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldCity;
    @FXML private TextField fieldYear;
    @FXML private TextField fieldDiscipline;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    // ─────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(
                FXCollections.observableArrayList(artistService.getAllDisciplines()));

        // Clic sur une ligne → remplit le formulaire
        artistTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> {
                    if (selected != null) fillForm(selected);
                });

        refreshTable();
    }

    // ── Recherche ─────────────────────────────────────────────────────────

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    @FXML
    private void handleAdd() {
        Artist artist = buildArtistFromForm();
        if (artist == null) return;

        artistService.createArtist(artist);
        refreshTable();
        clearForm();
        showInfo("Artiste ajouté", artist.getName() + " a été ajouté en base.");
    }

    @FXML
    private void handleUpdate() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Cliquez d'abord sur un artiste dans le tableau.");
            return;
        }
        Artist updated = buildArtistFromForm();
        if (updated == null) return;

        artistService.updateArtist(updated);
        refreshTable();
        clearForm();
        showInfo("Artiste modifié", updated.getName() + " a été mis à jour.");
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélection requise", "Cliquez d'abord sur un artiste dans le tableau.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + selected.getName() + " ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                artistService.deleteArtist(selected.getName());
                refreshTable();
                clearForm();
                showInfo("Supprimé", selected.getName() + " a été supprimé.");
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private Artist buildArtistFromForm() {
        String name = fieldName.getText().trim();
        String email = fieldEmail.getText().trim();
        if (name.isEmpty() || email.isEmpty()) {
            showWarning("Champs requis", "Le nom et l'email sont obligatoires.");
            return null;
        }
        Artist a = new Artist();
        a.setName(name);
        a.setContactEmail(email);
        a.setCity(fieldCity.getText().trim());
        if (!fieldDiscipline.getText().isBlank()) {
            a.getDisciplines().add(new Discipline(fieldDiscipline.getText().trim()));
        }
        try {
            if (!fieldYear.getText().isBlank())
                a.setBirthYear(Integer.parseInt(fieldYear.getText().trim()));
        } catch (NumberFormatException e) {
            showWarning("Format invalide", "L'année doit être un nombre entier.");
            return null;
        }
        return a;
    }

    private void fillForm(Artist a) {
        fieldName.setText(a.getName());
        fieldEmail.setText(a.getContactEmail() != null ? a.getContactEmail() : "");
        fieldCity.setText(a.getCity() != null ? a.getCity() : "");
        fieldYear.setText(a.getBirthYear() != null ? String.valueOf(a.getBirthYear()) : "");
        fieldDiscipline.setText(a.getDisciplines().isEmpty()
                ? "" : a.getDisciplines().get(0).getName());
    }

    private void clearForm() {
        fieldName.clear(); fieldEmail.clear(); fieldCity.clear();
        fieldYear.clear(); fieldDiscipline.clear();
        artistTable.getSelectionModel().clearSelection();
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