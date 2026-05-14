package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommunityController {

    @FXML private TableView<CommunityMember>           memberTable;
    @FXML private TableColumn<CommunityMember, String> nameColumn;
    @FXML private TableColumn<CommunityMember, String> emailColumn;
    @FXML private TableColumn<CommunityMember, String> cityColumn;

    @FXML private TextField fieldName;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldCity;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        refreshData();

        memberTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fieldName.setText(newSel.getName());
                fieldEmail.setText(newSel.getEmail());
                fieldCity.setText(newSel.getCity());
            }
        });
    }

    private void refreshData() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    @FXML
    public void handleAdd() {
        CommunityMember m = new CommunityMember();
        m.setName(fieldName.getText());
        m.setEmail(fieldEmail.getText());
        m.setCity(fieldCity.getText());

        communityService.saveMember(m);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleUpdate() {
        CommunityMember m = new CommunityMember();
        m.setName(fieldName.getText());
        m.setEmail(fieldEmail.getText());
        m.setCity(fieldCity.getText());

        communityService.updateMember(m);
        refreshData();
        clearForm();
    }

    @FXML
    public void handleDelete() {
        if (!fieldEmail.getText().isBlank()) {
            communityService.deleteMember(fieldEmail.getText());
            refreshData();
            clearForm();
        }
    }

    private void clearForm() {
        fieldName.clear();
        fieldEmail.clear();
        fieldCity.clear();
    }
}
