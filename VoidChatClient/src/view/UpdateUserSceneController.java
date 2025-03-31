package view;

import controller.ClientController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;
import javafx.scene.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.event.ActionEvent;

public class UpdateUserSceneController {

    @FXML
    private TextField txtFieldFirstName;
    @FXML
    private TextField txtFieldLastName;
    @FXML
    private TextField txtFieldEmail;
    @FXML
    private PasswordField txtFieldPassword;
    @FXML
    private RadioButton radioMale;
    @FXML
    private RadioButton radioFemale;
    @FXML
    private ComboBox<String> comboBoxCountry;
    @FXML
    private ImageView imageViewProfile;

    private ClientController clientController;
    private User currentUser;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        // Initialize country combo box with some common countries
        List<String> countries = new ArrayList<>();
        countries.add("United States");
        countries.add("United Kingdom");
        countries.add("Canada");
        countries.add("Australia");
        countries.add("Germany");
        countries.add("France");
        countries.add("Japan");
        countries.add("China");
        countries.add("India");
        countries.add("Brazil");
        comboBoxCountry.getItems().addAll(countries);

        // Load current user data
        clientController = ClientController.getInstance();
        currentUser = clientController.getUserInformation();
        if (currentUser != null) {
            txtFieldFirstName.setText(currentUser.getFname());
            txtFieldLastName.setText(currentUser.getLname());
            txtFieldEmail.setText(currentUser.getEmail());
            if (currentUser.getGender().equals("Male")) {
                radioMale.setSelected(true);
            } else {
                radioFemale.setSelected(true);
            }
            comboBoxCountry.setValue(currentUser.getCountry());

            // Load profile image if exists
            if (currentUser.getImage() != null) {
                try {
                    Image image = new Image(new FileInputStream(currentUser.getImage()));
                    imageViewProfile.setImage(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    private void chooseImageAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedImageFile));
                imageViewProfile.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void saveBtnAction() {
        // Validate inputs
        if (txtFieldFirstName.getText().trim().isEmpty() ||
                txtFieldLastName.getText().trim().isEmpty() ||
                txtFieldEmail.getText().trim().isEmpty() ||
                txtFieldPassword.getText().trim().isEmpty() ||
                (!radioMale.isSelected() && !radioFemale.isSelected()) ||
                comboBoxCountry.getValue() == null) {

            showAlert("Error", "Please fill in all fields");
            return;
        }

        // Update user object
        currentUser.setFname(txtFieldFirstName.getText().trim());
        currentUser.setLname(txtFieldLastName.getText().trim());
        currentUser.setEmail(txtFieldEmail.getText().trim());
        currentUser.setPassword(txtFieldPassword.getText().trim());
        currentUser.setGender(radioMale.isSelected() ? "Male" : "Female");
        currentUser.setCountry(comboBoxCountry.getValue());

        if (selectedImageFile != null) {
            currentUser.setImage(selectedImageFile.getAbsolutePath());
        }

        // Save changes
        if (clientController.updateUser(currentUser)) {
            showAlert("Success", "Profile updated successfully");
            closeDialog();
        } else {
            showAlert("Error", "Failed to update profile");
        }
    }

    @FXML
    private void cancelBtnAction() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtFieldFirstName.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void btnUpdateAction(ActionEvent event) {
        // Get current user info
        User currentUser = clientController.getUserInformation();

        // Update user info
        currentUser.setEmail(txtFieldEmail.getText());
        currentUser.setFname(txtFieldFirstName.getText());
        currentUser.setLname(txtFieldLastName.getText());
        currentUser.setGender(radioMale.isSelected() ? "Male" : "Female");
        currentUser.setCountry(comboBoxCountry.getValue());

        // Update user in database
        if (clientController.updateUser(currentUser)) {
            showAlert("Success", "Profile updated successfully");
            closeDialog();
        } else {
            showAlert("Error", "Failed to update profile");
        }
    }

    @FXML
    private void btnSelectImageAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Copy the selected image to resources folder
                String imageName = "profile_" + clientController.getUserInformation().getUsername() +
                        selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                String destPath = getClass().getResource("/resouces/").getPath() + imageName;
                Files.copy(selectedFile.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);

                // Update user's image path
                User currentUser = clientController.getUserInformation();
                currentUser.setImage("/resouces/" + imageName);

                // Update user in database
                if (clientController.updateUser(currentUser)) {
                    // Update image view
                    imageViewProfile.setImage(new Image(currentUser.getImage()));
                    showAlert("Success", "Profile image updated successfully");
                } else {
                    showAlert("Error", "Failed to update profile image");
                }
            } catch (IOException ex) {
                showAlert("Error", "Failed to save image file");
            }
        }
    }
}