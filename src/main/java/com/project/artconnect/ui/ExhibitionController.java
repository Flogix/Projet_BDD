package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExhibitionController {

    @FXML private TableView<Exhibition> exhibitionTable;
    @FXML private TableColumn<Exhibition, String> titleColumn;
    @FXML private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML private TableColumn<Exhibition, String> themeColumn;
    @FXML private TableColumn<Exhibition, String> galleryColumn;

    @FXML private TextField fieldTitle;
    @FXML private TextField fieldGallery;
    @FXML private DatePicker fieldStartDate;
    @FXML private TextField fieldTheme;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));

        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));

        refreshData();

        exhibitionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fieldTitle.setText(newSel.getTitle());
                fieldStartDate.setValue(newSel.getStartDate());
                fieldTheme.setText(newSel.getTheme());
                fieldGallery.setText(newSel.getGallery() != null ? newSel.getGallery().getName() : "");
            }
        });
    }

    private void refreshData() {
        exhibitionTable.setItems(FXCollections.observableArrayList(galleryService.getAllExhibitions()));
    }

    @FXML
    public void handleAdd() {
        Exhibition e = new Exhibition();
        e.setTitle(fieldTitle.getText());
        e.setStartDate(fieldStartDate.getValue());
        e.setTheme(fieldTheme.getText());
        
        String gName = fieldGallery.getText();
        if (gName != null && !gName.isBlank()) {
            Gallery g = galleryService.getGalleryByName(gName).orElse(new Gallery(gName, "N/A", 0.0));
            e.setGallery(g);
        }
        
        galleryService.saveExhibition(e);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleUpdate() {
        Exhibition e = new Exhibition();
        e.setTitle(fieldTitle.getText());
        e.setStartDate(fieldStartDate.getValue());
        e.setTheme(fieldTheme.getText());
        
        String gName = fieldGallery.getText();
        if (gName != null && !gName.isBlank()) {
            Gallery g = galleryService.getGalleryByName(gName).orElse(new Gallery(gName, "N/A", 0.0));
            e.setGallery(g);
        }
        
        galleryService.updateExhibition(e);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleDelete() {
        if (!fieldTitle.getText().isBlank()) {
            galleryService.deleteExhibition(fieldTitle.getText());
            refreshData();
            clearForm();
        }
    }

    private void clearForm() {
        fieldTitle.clear();
        fieldTheme.clear();
        fieldGallery.clear();
        fieldStartDate.setValue(null);
    }
}
