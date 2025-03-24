package view;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.User;

public class UpdateProfileBoxController implements Initializable {

    @FXML
    private VBox updateProfileBox;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtFname;
    @FXML
    private TextField txtLname;
    @FXML
    private ComboBox<String> comboGender;
    @FXML
    private TextField txtCountry;
    @FXML
    private Button btnUpdate;

    private ClientView clientView;
    private User currentUser;

    public UpdateProfileBoxController() {
        clientView = ClientView.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize gender combo box
        comboGender.getItems().addAll("Female", "Male");

        // Load current user data
        currentUser = clientView.getUserInformation();
        if (currentUser != null) {
            txtEmail.setText(currentUser.getEmail());
            txtFname.setText(currentUser.getFname());
            txtLname.setText(currentUser.getLname());
            comboGender.setValue(currentUser.getGender());
            txtCountry.setText(currentUser.getCountry());
        }
    }

    @FXML
    private void handleUpdateAction(ActionEvent event) {
        // Create updated user object
        User updatedUser = new User(
                currentUser.getUsername(),
                txtEmail.getText(),
                txtFname.getText(),
                txtLname.getText(),
                currentUser.getPassword(),
                comboGender.getValue(),
                txtCountry.getText());

        // Update user information
        boolean success = clientView.updateUser(updatedUser);
        if (success) {
            clientView.showSuccess("Success", "Profile Updated", "Your profile has been updated successfully.");
        } else {
            clientView.showError("Error", "Update Failed", "Failed to update your profile. Please try again.");
        }
    }
}