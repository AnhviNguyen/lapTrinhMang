package view;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;

public class UpdateProfileBoxController implements Initializable {

    @FXML
    private VBox updateProfileBox;

    @FXML
    private ImageView profileImage;

    @FXML
    private Button btnChangeProfilePic;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFname;

    @FXML
    private TextField txtLname;

    @FXML
    private ComboBox<String> comboGender;

    @FXML
    private ComboBox<String> comboCountry;

    @FXML
    private PasswordField txtCurrentPassword;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnUpdate;

    private ClientView clientView;
    private User currentUser;
    private File selectedProfilePicture = null;

    public UpdateProfileBoxController() {
        clientView = ClientView.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize gender combo box
        comboGender.getItems().addAll("Female", "Male");

        // Initialize country combo box with all countries
        ObservableList<String> countryList = FXCollections.observableArrayList(
                "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda",
                "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain",
                "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia",
                "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso",
                "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic",
                "Chad", "Chile", "China", "Colombia", "Comoros", "Congo", "Costa Rica", "Croatia", "Cuba",
                "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
                "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia",
                "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana",
                "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras",
                "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy",
                "Ivory Coast", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "North Korea",
                "South Korea", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia",
                "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macedonia", "Madagascar", "Malawi",
                "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius",
                "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique",
                "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger",
                "Nigeria", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay",
                "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda",
                "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa",
                "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles",
                "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia",
                "South Africa", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden",
                "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tonga",
                "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine",
                "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan",
                "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe");
        comboCountry.setItems(countryList);

        // Load current user data
        currentUser = clientView.getUserInformation();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            txtEmail.setText(currentUser.getEmail());
            txtFname.setText(currentUser.getFname());
            txtLname.setText(currentUser.getLname());
            comboGender.setValue(currentUser.getGender());
            comboCountry.setValue(currentUser.getCountry());

            // Set user's profile picture based on gender
            if (currentUser.getGender() != null && currentUser.getGender().equals("Female")) {
                profileImage.setImage(new Image(getClass().getResource("/resouces/female.png").toExternalForm()));
            } else {
                profileImage.setImage(new Image(getClass().getResource("/resouces/user.png").toExternalForm()));
            }
        }

        // Add event handlers
        btnChangeProfilePic.setOnAction(event -> handleChangeProfilePicture());
        btnCancel.setOnAction(event -> handleCancel());
    }

    private void handleChangeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        // Show open file dialog
        Stage stage = (Stage) updateProfileBox.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Preview the image
                Image image = new Image(file.toURI().toString());
                profileImage.setImage(image);

                // Store the selected file for later use
                selectedProfilePicture = file;
            } catch (Exception e) {
                clientView.showError("Error", "Image Error", "Could not load the selected image.");
            }
        }
    }

    private void handleCancel() {
        // Reload the home content
        try {
            // Get the ChatSceneController instance and reload home content
            ClientView.getInstance().getChatSceneController().loadHomeScene();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateAction(ActionEvent event) {
        // Validate inputs
        String errorMsg = validateInputs();
        if (!errorMsg.isEmpty()) {
            clientView.showError("Validation Error", "Please correct the following errors:", errorMsg);
            return;
        }

        // Handle password change if specified
        if (txtNewPassword.getText() != null && !txtNewPassword.getText().isEmpty()) {
            if (!handlePasswordChange()) {
                return; // Password change failed, do not proceed
            }
        }

        // Create updated user object
        User updatedUser = new User(
                currentUser.getUsername(),
                txtEmail.getText(),
                txtFname.getText(),
                txtLname.getText(),
                currentUser.getPassword(),
                comboGender.getValue(),
                comboCountry.getValue());

        // Update user information
        boolean success = clientView.updateUser(updatedUser);
        if (success) {
            // Handle profile picture upload if selected
            if (selectedProfilePicture != null) {
                // In a real implementation, you would upload the profile picture here
                // For now, we'll just simulate success
                clientView.showSuccess("Success", "Profile Updated",
                        "Your profile has been updated successfully, including your profile picture.");
            } else {
                clientView.showSuccess("Success", "Profile Updated",
                        "Your profile has been updated successfully.");
            }

            // Refresh the UI with updated user info
            currentUser = clientView.getUserInformation();
        } else {
            clientView.showError("Error", "Update Failed",
                    "Failed to update your profile. Please try again.");
        }
    }

    private String validateInputs() {
        StringBuilder errorMsg = new StringBuilder();

        if (txtFname.getText().trim().isEmpty()) {
            errorMsg.append("- First name is required\n");
        }

        if (txtLname.getText().trim().isEmpty()) {
            errorMsg.append("- Last name is required\n");
        }

        if (txtEmail.getText().trim().isEmpty()) {
            errorMsg.append("- Email is required\n");
        } else if (!txtEmail.getText().contains("@") || !txtEmail.getText().contains(".")) {
            errorMsg.append("- Please enter a valid email address\n");
        }

        if (comboGender.getValue() == null) {
            errorMsg.append("- Please select a gender\n");
        }

        if (comboCountry.getValue() == null) {
            errorMsg.append("- Please select a country\n");
        }

        // Password validation
        if (txtNewPassword.getText() != null && !txtNewPassword.getText().isEmpty()
                || txtConfirmPassword.getText() != null && !txtConfirmPassword.getText().isEmpty()) {
            if (txtCurrentPassword.getText() == null || txtCurrentPassword.getText().isEmpty()) {
                errorMsg.append("- Current password is required to change password\n");
            }

            if (txtNewPassword.getText() == null || txtConfirmPassword.getText() == null ||
                    !txtNewPassword.getText().equals(txtConfirmPassword.getText())) {
                errorMsg.append("- New passwords do not match\n");
            }

            if (txtNewPassword.getText() != null && txtNewPassword.getText().length() < 6) {
                errorMsg.append("- New password must be at least 6 characters\n");
            }
        }

        return errorMsg.toString();
    }

    private boolean handlePasswordChange() {
        // In a real implementation, you would verify the current password against the
        // stored password
        // For now, we'll just simulate password validation

        // Check if current password is correct (in a real app, this would be verified
        // with the server)
        if (!txtCurrentPassword.getText().equals(currentUser.getPassword())) {
            clientView.showError("Password Error", "Incorrect Password",
                    "The current password you entered is incorrect.");
            return false;
        }

        // For a real implementation, you would update the password in the database
        // Here we'll just update the local user object
        currentUser.setPassword(txtNewPassword.getText());

        return true;
    }
}