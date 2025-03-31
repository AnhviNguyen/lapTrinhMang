package view;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;

public class CreateGroupSceneController implements Initializable {
    @FXML
    private TextField txtFieldGroupName;

    @FXML
    private TextField txtFieldSearch;

    @FXML
    private ListView<HBox> listViewFriends;

    @FXML
    private FlowPane flowPaneSelectedMembers;

    @FXML
    private Label lblNoMembers;

    @FXML
    private Button btnCreateGroup;

    private ClientView clientView;
    private ArrayList<String> selectedMembers = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientView = ClientView.getInstance();

        // Allow multiple selection in ListView
        listViewFriends.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Load friends list
        loadFriends();

        // Disable Create Group button initially
        updateCreateButtonState();
    }

    private void loadFriends() {
        friends = clientView.getContacts();
        if (friends != null) {
            listViewFriends.getItems().clear();

            for (User friend : friends) {
                // Skip group entries
                if (friend.getUsername().contains("##"))
                    continue;

                HBox friendBox = createFriendListItem(friend);
                listViewFriends.getItems().add(friendBox);
            }
        }
    }

    private HBox createFriendListItem(User friend) {
        HBox friendBox = new HBox(10);
        friendBox.setAlignment(Pos.CENTER_LEFT);
        friendBox.setPadding(new Insets(8));
        friendBox.setUserData(friend.getUsername()); // Store username for later use

        // Add hover effect
        friendBox.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;");
        friendBox
                .setOnMouseEntered(e -> friendBox.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 4;"));
        friendBox.setOnMouseExited(
                e -> friendBox.setStyle("-fx-background-color: transparent; -fx-background-radius: 4;"));

        // Avatar placeholder (circle with color based on name)
        Circle avatar = new Circle(15);
        avatar.setFill(getColorFromName(friend.getFname()));

        // Create CheckBox for selection
        CheckBox checkBox = new CheckBox();

        // Name and details
        VBox nameBox = new VBox(2);
        Label nameLabel = new Label(friend.getFname() + " " + friend.getLname());
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + friend.getUsername());
        usernameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #65676b;");
        nameBox.getChildren().addAll(nameLabel, usernameLabel);

        friendBox.getChildren().addAll(checkBox, avatar, nameBox);

        // Handle checkbox selection
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            String username = (String) friendBox.getUserData();
            if (newVal) {
                if (!selectedMembers.contains(username)) {
                    selectedMembers.add(username);
                    addSelectedMemberChip(friend);
                }
            } else {
                selectedMembers.remove(username);
                removeSelectedMemberChip(username);
            }
            updateCreateButtonState();
        });

        return friendBox;
    }

    private void addSelectedMemberChip(User friend) {
        // Hide "No members selected" label if visible
        lblNoMembers.setVisible(false);
        lblNoMembers.setManaged(false);

        // Create member chip
        HBox memberChip = new HBox(5);
        memberChip.setAlignment(Pos.CENTER_LEFT);
        memberChip.setPadding(new Insets(5, 10, 5, 10));
        memberChip.setStyle(
                "-fx-background-color: #e7f3ff; -fx-background-radius: 20; -fx-border-color: #0866ff; -fx-border-radius: 20; -fx-border-width: 1;");
        memberChip.setUserData(friend.getUsername()); // Store username for removal

        // Member name
        Label nameLabel = new Label(friend.getFname() + " " + friend.getLname());
        nameLabel.setStyle("-fx-text-fill: #0866ff; -fx-font-weight: bold;");

        // Remove button
        Label removeBtn = new Label("×");
        removeBtn.setStyle("-fx-text-fill: #0866ff; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setTextAlignment(TextAlignment.CENTER);

        // Add remove action
        removeBtn.setOnMouseClicked(e -> {
            String username = (String) memberChip.getUserData();
            selectedMembers.remove(username);
            flowPaneSelectedMembers.getChildren().remove(memberChip);

            // Update checkboxes in the list view
            for (HBox item : listViewFriends.getItems()) {
                if (username.equals(item.getUserData())) {
                    CheckBox checkBox = (CheckBox) item.getChildren().get(0);
                    checkBox.setSelected(false);
                    break;
                }
            }

            // Show "No members selected" label if needed
            if (selectedMembers.isEmpty()) {
                lblNoMembers.setVisible(true);
                lblNoMembers.setManaged(true);
            }

            updateCreateButtonState();
        });

        memberChip.getChildren().addAll(nameLabel, removeBtn);
        flowPaneSelectedMembers.getChildren().add(memberChip);
    }

    private void removeSelectedMemberChip(String username) {
        for (int i = 0; i < flowPaneSelectedMembers.getChildren().size(); i++) {
            if (flowPaneSelectedMembers.getChildren().get(i) instanceof HBox) {
                HBox memberChip = (HBox) flowPaneSelectedMembers.getChildren().get(i);
                if (username.equals(memberChip.getUserData())) {
                    flowPaneSelectedMembers.getChildren().remove(memberChip);
                    break;
                }
            }
        }

        // Show "No members selected" label if needed
        if (selectedMembers.isEmpty()) {
            lblNoMembers.setVisible(true);
            lblNoMembers.setManaged(true);
        }
    }

    private Color getColorFromName(String name) {
        if (name == null || name.isEmpty()) {
            return Color.GRAY;
        }
        // Generate a color based on the first character
        int hash = name.charAt(0);
        int r = (hash * 123) % 200 + 55; // Avoid too dark/light colors
        int g = (hash * 321) % 200 + 55;
        int b = (hash * 213) % 200 + 55;
        return Color.rgb(r, g, b);
    }

    private void updateCreateButtonState() {
        // Enable create button only if group name is set and at least one member is
        // selected
        boolean hasName = txtFieldGroupName.getText() != null && !txtFieldGroupName.getText().trim().isEmpty();
        boolean hasMembers = !selectedMembers.isEmpty();
        btnCreateGroup.setDisable(!(hasName && hasMembers));
    }

    @FXML
    private void searchAction(ActionEvent event) {
        String searchText = txtFieldSearch.getText().toLowerCase();
        if (searchText.isEmpty()) {
            // If search field is empty, show all
            for (HBox friendBox : listViewFriends.getItems()) {
                friendBox.setVisible(true);
                friendBox.setManaged(true);
            }
        } else {
            // Otherwise, filter based on name or username
            for (HBox friendBox : listViewFriends.getItems()) {
                String username = (String) friendBox.getUserData();

                // Find the user
                User friend = null;
                for (User u : friends) {
                    if (u.getUsername().equals(username)) {
                        friend = u;
                        break;
                    }
                }

                if (friend != null) {
                    String fullName = (friend.getFname() + " " + friend.getLname()).toLowerCase();
                    boolean matches = fullName.contains(searchText) ||
                            friend.getUsername().toLowerCase().contains(searchText);

                    friendBox.setVisible(matches);
                    friendBox.setManaged(matches);
                }
            }
        }
    }

    // Listen for changes in the group name field
    @FXML
    private void onGroupNameChange() {
        updateCreateButtonState();
    }

    @FXML
    private void createGroupBtnAction(ActionEvent event) {
        String groupName = txtFieldGroupName.getText();
        if (groupName == null || groupName.trim().isEmpty()) {
            clientView.showError("Error", "Invalid Group Name", "Please enter a group name");
            return;
        }

        if (selectedMembers.isEmpty()) {
            clientView.showError("Error", "No Members Selected", "Please select at least one member for the group");
            return;
        }

        // Add current user to the group
        String currentUsername = clientView.getUserInformation().getUsername();
        if (!selectedMembers.contains(currentUsername)) {
            selectedMembers.add(currentUsername);
        }

        // Create group with "##" prefix
        String formattedGroupName = "##" + groupName;
        clientView.createGroup(formattedGroupName, selectedMembers);

        // Show success message
        clientView.showSuccess("Success", "Group Created",
                "Group '" + groupName + "' has been created with " + selectedMembers.size() + " members");

        // Close the window
        ((Stage) txtFieldGroupName.getScene().getWindow()).close();
    }

    @FXML
    private void cancelBtnAction(ActionEvent event) {
        ((Stage) txtFieldGroupName.getScene().getWindow()).close();
    }
}