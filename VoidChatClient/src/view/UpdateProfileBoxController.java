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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.User;
import javafx.application.Platform;

public class UpdateProfileBoxController implements Initializable {

    @FXML
    private VBox updateProfileBox;

    @FXML
    private ImageView profileImage;

    @FXML
    private Button btnChangeProfilePic;

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
            txtEmail.setText(currentUser.getEmail());
            txtFname.setText(currentUser.getFname());
            txtLname.setText(currentUser.getLname());
            comboGender.setValue(currentUser.getGender());
            comboCountry.setValue(currentUser.getCountry());

            // Set user's profile picture with custom or default image
            try {
                // Xóa clip hình tròn - hiển thị hình ảnh đầy đủ
                profileImage.setClip(null);

                // Xóa bỏ các hiệu ứng style, chỉ giữ border đơn giản
                profileImage.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");

                // Đặt lại style mặc định cho container ảnh
                StackPane parent = (StackPane) profileImage.getParent();
                if (parent != null) {
                    parent.setStyle("-fx-background-color: white;");
                }

                // Load the appropriate image based on availability
                if (currentUser.getImage() != null && !currentUser.getImage().trim().isEmpty()) {
                    // User has a custom avatar - use it with cache disabled
                    Image userImage = new Image(currentUser.getImage(), false);
                    profileImage.setImage(userImage);
                    profileImage.setCache(false);
                    System.out.println("Loaded custom avatar: " + currentUser.getImage());
                } else {
                    // Use default avatar based on gender
                    String defaultImage = (currentUser.getGender() != null &&
                            currentUser.getGender().equalsIgnoreCase("Female")) ? "/resouces/female.png"
                                    : "/resouces/user.png";

                    // Add timestamp to force cache refresh
                    String imagePath = getClass().getResource(defaultImage).toExternalForm() +
                            "?t=" + System.currentTimeMillis();

                    profileImage.setImage(new Image(imagePath, false));
                    System.out.println("Loaded default avatar based on gender");
                }

                // Ensure the imageView preserves ratio
                profileImage.setPreserveRatio(true);
                profileImage.setSmooth(true);
                profileImage.setCache(false);

                // Đảm bảo kích thước phù hợp
                profileImage.setFitWidth(120);
                profileImage.setFitHeight(120);

            } catch (Exception e) {
                System.out.println("Error loading avatar: " + e.getMessage());
                // Fallback to default image if there's an error
                try {
                    profileImage.setImage(new Image(
                            getClass().getResource("/resouces/user.png").toExternalForm()));
                } catch (Exception ex) {
                    System.out.println("Could not load fallback avatar");
                }
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
                // Load image with caching disabled
                Image image = new Image(file.toURI().toString(), false);
                profileImage.setImage(image);
                profileImage.setCache(false);

                // Xóa clip để hiển thị hình đầy đủ
                profileImage.setClip(null);

                // Chỉ đặt border đơn giản
                profileImage.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");

                // Đặt style đơn giản cho container
                StackPane parent = (StackPane) profileImage.getParent();
                if (parent != null) {
                    parent.setStyle("-fx-background-color: white;");
                }

                // Đảm bảo hiển thị hình ảnh đầy đủ
                profileImage.setPreserveRatio(true);
                profileImage.setSmooth(true);

                // Đảm bảo kích thước phù hợp
                profileImage.setFitWidth(120);
                profileImage.setFitHeight(120);

                // Store the selected file for later use
                selectedProfilePicture = file;
                System.out.println("New profile picture selected: " + file.getAbsolutePath());
            } catch (Exception e) {
                clientView.showError("Error", "Image Error", "Could not load the selected image.");
                e.printStackTrace();
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

        // Set image field if a new profile picture was selected
        boolean avatarChanged = false;
        if (selectedProfilePicture != null) {
            try {
                // Convert file to a URL format that can be loaded by JavaFX
                String imageUrl = selectedProfilePicture.toURI().toString();
                System.out.println("Setting new profile image: " + imageUrl);
                updatedUser.setImage(imageUrl);
                avatarChanged = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                clientView.showError("Error", "Image Error", "Could not process the profile picture.");
                return;
            }
        } else {
            // Keep existing image if no new one was selected
            updatedUser.setImage(currentUser.getImage());
        }

        // Update user information
        boolean success = clientView.updateUser(updatedUser);
        if (success) {
            // Update success message based on whether a profile picture was updated
            if (avatarChanged) {
                clientView.showSuccess("Success", "Profile Updated",
                        "Your profile has been updated successfully, including your profile picture.");
                System.out.println("Profile picture updated for user: " + currentUser.getUsername());
            } else {
                clientView.showSuccess("Success", "Profile Updated",
                        "Your profile has been updated successfully.");
            }

            // Get fresh user data after update
            currentUser = clientView.getUserInformation();
            final String username = currentUser.getUsername();

            // Force comprehensive avatar refresh across the application
            try {
                System.out.println("Starting comprehensive avatar refresh for: " + username);

                // Call the new comprehensive avatar refresh method that updates all UI
                if (clientView.getChatSceneController() != null) {
                    clientView.getChatSceneController().refreshAllAvatars(username);
                }

                // First refresh immediately through ClientView's method
                clientView.forceAvatarRefresh(username);

                // Sequence of delayed refreshes with increasing intervals for reliable updates
                new Thread(() -> {
                    try {
                        // First delayed refresh after 500ms
                        Thread.sleep(500);
                        javafx.application.Platform.runLater(() -> {
                            if (clientView.getChatSceneController() != null) {
                                // Use the comprehensive method that targets all UI elements
                                clientView.getChatSceneController().refreshAllAvatars(username);
                            }
                        });

                        // Final refresh after 2 seconds to catch any late UI updates
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            if (clientView.getChatSceneController() != null) {
                                // Special focus on TitledPane components
                                clientView.getChatSceneController().refreshAccordionAvatars();

                                // Final tab avatar update
                                clientView.getChatSceneController().updateFriendTabIcon(username);
                            }
                            // Full refresh one last time
                            clientView.forceAvatarRefresh(username);
                        });
                    } catch (Exception e) {
                        System.out.println("Error in delayed avatar refresh: " + e.getMessage());
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception ex) {
                System.out.println("Error during avatar refresh sequence: " + ex.getMessage());
                ex.printStackTrace();
            }

            // Return to home screen
            handleCancel();
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

    /**
     * Handle the save profile button
     * - Updates the user's profile information
     * - Shows success message
     * - Updates UI elements
     * 
     * @param event
     */
    @FXML
    private void handleSaveProfile(ActionEvent event) {
        System.out.println("Saving profile updates...");

        try {
            // Get the current user information
            User currentUser = clientView.getUserInformation();

            // Update user properties with form values
            currentUser.setFname(txtFname.getText());
            currentUser.setLname(txtLname.getText());
            currentUser.setEmail(txtEmail.getText());
            currentUser.setPassword(txtNewPassword.getText());
            currentUser.setCountry(comboCountry.getValue());
            currentUser.setGender(comboGender.getValue());

            // Set the updated image path if we have a new one
            if (selectedProfilePicture != null && !selectedProfilePicture.toURI().toString().isEmpty()) {
                currentUser.setImage(selectedProfilePicture.toURI().toString());
            }

            // Save changes to the server
            boolean updated = clientView.updateUser(currentUser);

            if (updated) {
                // Show success message
                clientView.showSuccess("Success", "Profile Updated",
                        "Your profile has been updated successfully");

                // Force refresh user avatar in main interface
                Platform.runLater(() -> {
                    try {
                        // Explicitly request UI update for the avatar in the header
                        ChatSceneController chatController = clientView.getChatSceneController();
                        if (chatController != null) {
                            System.out.println("Refreshing user profile image in header...");
                            chatController.refreshUserProfileImage();

                            // Also refresh contacts to show updated avatar everywhere
                            chatController.refreshContacts();
                        }
                    } catch (Exception e) {
                        System.out.println("Error refreshing UI after profile update: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                clientView.showError("Error", "Update Failed",
                        "Failed to update your profile. Please try again later.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            clientView.showError("Error", "Update Error",
                    "An error occurred while updating your profile.");
        }
    }
}