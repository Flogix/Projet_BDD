package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class WorkshopController {

    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;

    @FXML private TextField fieldTitle;
    @FXML private TextField fieldInstructor;
    @FXML private DatePicker fieldDate;
    @FXML private TextField fieldPrice;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName() : "Unknown"));

        refreshData();

        workshopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fieldTitle.setText(newSel.getTitle());
                if (newSel.getDate() != null) {
                    fieldDate.setValue(newSel.getDate().toLocalDate());
                } else {
                    fieldDate.setValue(null);
                }
                fieldPrice.setText(String.valueOf(newSel.getPrice()));
                fieldInstructor.setText(newSel.getInstructor() != null ? newSel.getInstructor().getName() : "");
            }
        });
    }

    private void refreshData() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    @FXML
    public void handleAdd() {
        Workshop w = new Workshop();
        w.setTitle(fieldTitle.getText());
        if (fieldDate.getValue() != null) {
            w.setDate(LocalDateTime.of(fieldDate.getValue(), LocalTime.of(10, 0))); // default time
        }
        w.setPrice(Double.parseDouble(fieldPrice.getText() == null || fieldPrice.getText().isBlank() ? "0.0" : fieldPrice.getText()));
        
        String instructorName = fieldInstructor.getText();
        if (instructorName != null && !instructorName.isBlank()) {
            Artist a = new Artist();
            a.setName(instructorName);
            w.setInstructor(a);
        }
        
        workshopService.saveWorkshop(w);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleUpdate() {
        Workshop w = new Workshop();
        w.setTitle(fieldTitle.getText());
        if (fieldDate.getValue() != null) {
            w.setDate(LocalDateTime.of(fieldDate.getValue(), LocalTime.of(10, 0))); 
        }
        w.setPrice(Double.parseDouble(fieldPrice.getText() == null || fieldPrice.getText().isBlank() ? "0.0" : fieldPrice.getText()));
        
        String instructorName = fieldInstructor.getText();
        if (instructorName != null && !instructorName.isBlank()) {
            Artist a = new Artist();
            a.setName(instructorName);
            w.setInstructor(a);
        }
        
        workshopService.updateWorkshop(w);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleDelete() {
        if (!fieldTitle.getText().isBlank()) {
            workshopService.deleteWorkshop(fieldTitle.getText());
            refreshData();
            clearForm();
        }
    }

    private void clearForm() {
        fieldTitle.clear();
        fieldInstructor.clear();
        fieldPrice.clear();
        fieldDate.setValue(null);
    }

    @FXML
    public void handleCalculateRevenue() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun atelier sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un atelier dans la liste.");
            alert.showAndWait();
            return;
        }

        // Appel de la méthode du service qui va requêter la base de données
        double maxRevenue = workshopService.calculateMaxRevenue(selected.getTitle());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Revenu Maximum Estimé");
        alert.setHeaderText("Atelier : " + selected.getTitle());
        alert.setContentText("Le revenu a été calculé en base de données : " + maxRevenue + " €\n" +
                             "Cette fonctionnalité démontre l'appel de requête SQL personnalisée (ou fonction).");
        alert.showAndWait();
    }
}
