package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class GalleryController {

    @FXML private TableView<Gallery> galleryTable;
    @FXML private TableColumn<Gallery, String> nameColumn;
    @FXML private TableColumn<Gallery, String> addressColumn;
    @FXML private TableColumn<Gallery, Double> ratingColumn;

    @FXML private TextField fieldName;
    @FXML private TextField fieldAddress;
    @FXML private TextField fieldRating;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        refreshData();

        galleryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fieldName.setText(newSel.getName());
                fieldAddress.setText(newSel.getAddress());
                fieldRating.setText(String.valueOf(newSel.getRating()));
            }
        });
    }

    private void refreshData() {
        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    @FXML
    public void handleAdd() {
        Gallery g = new Gallery(fieldName.getText(), fieldAddress.getText(), 
                Double.parseDouble(fieldRating.getText() == null || fieldRating.getText().isBlank() ? "0.0" : fieldRating.getText()));
        galleryService.saveGallery(g);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleUpdate() {
        Gallery g = new Gallery(fieldName.getText(), fieldAddress.getText(), 
                Double.parseDouble(fieldRating.getText() == null || fieldRating.getText().isBlank() ? "0.0" : fieldRating.getText()));
        galleryService.updateGallery(g);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleDelete() {
        if (!fieldName.getText().isBlank()) {
            galleryService.deleteGallery(fieldName.getText());
            refreshData();
            clearForm();
        }
    }

    private void clearForm() {
        fieldName.clear();
        fieldAddress.clear();
        fieldRating.clear();
    }
}
