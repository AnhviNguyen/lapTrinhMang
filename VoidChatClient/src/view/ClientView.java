package view;

import controller.ClientController;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.ClientModelInt;
import model.Message;
import model.User;
import model.VoiceMessage;
import utilitez.Notification;
import utilitez.Pair;

/**
 *
 * @author Merna
 */
public class ClientView extends Application implements ClientViewInt {

    private ClientController controller;
    private static ClientView instance;
    private static Stage mainStage;
    public Label serverStatusLabel;

    // 2na 2le 3amlaha
    public view.ChatBoxController chatBoxController;

    // views Controller
    ChatSceneController chatSceneController;
    // HomeBox Controller
    HomeBoxController homeBoxController;

    // Map to store chat box controllers
    private Map<String, ChatBoxController> tabsControllers = new HashMap<>();

    public ClientView() {
        controller = new ClientController(this);
        instance = this;

    }

    /**
     * get static instance form client view
     *
     * @return ClientView instance
     */
    public static ClientView getInstance() {
        return instance;
    }

    public void setChatSceneController(ChatSceneController chatSceneController) {
        this.chatSceneController = chatSceneController;
    }

    public void setHomeBoxController(HomeBoxController homeBoxController) {
        this.homeBoxController = homeBoxController;
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("LaunchScene.fxml"));
        Scene scene = new Scene(root);
        stage.setOnCloseRequest((WindowEvent ew) -> {
            Platform.exit();
            // TODO : why not close
            System.exit(0);
        });
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    @Override
    public boolean signup(User user) throws Exception {

        return controller.signup(user);

    }

    @Override
    public User signin(String username, String password) throws Exception {
        return controller.signin(username, password);
    }

    /////////////////////////////////////////////////////////////////////

    @Override
    public void changeStatus(String status) {
        // Only proceed if the status is actually changing from the current user's
        // status
        User currentUser = getUserInformation();
        if (currentUser != null && !status.equals(currentUser.getStatus())) {
            // Update user information locally first to prevent feedback loop
            currentUser.setStatus(status);

            // Update local UI immediately
            Platform.runLater(() -> {
                // Update status in chat scene if it's open
                if (chatSceneController != null) {
                    chatSceneController.updateContactStatus(getUserInformation().getUsername(), status);
                }

                // Update any open chat boxes
                if (tabsControllers != null) {
                    for (ChatBoxController controller : tabsControllers.values()) {
                        if (controller != null) {
                            controller.updateFriendStatus(getUserInformation().getUsername(), status);
                        }
                    }
                }
            });

            // Then notify server to update other clients
            controller.changeStatus(status);
        }
    }
    /////////////////////////////////////////////////////////////////////

    @Override
    public void logout() {
        controller.logout();
    }

    @Override
    public int sendRequest(String friend, String category) {
        return controller.sendRequest(friend, category);

    }

    @Override
    public void notify(String message, int type) {
        Platform.runLater(() -> {
            switch (type) {
                case Notification.FRIEND_REQUSET:
                    showFriendRequest(message);
                    break;
                case Notification.ACCEPT_FRIEND_REQUEST:
                    showAcceptFriendRequest(message);
                    break;
                case Notification.FRIEND_ONLINE:
                    showFriendOnline(message);
                    break;
                case Notification.FRIEND_OFFLINE:
                    showFriendOffline(message);
                    break;
                case Notification.FRIEND_BUSY:
                    showFriendBusy(message);
                    break;
                case Notification.STATUS_UPDATE:
                    // Handle immediate status update notification
                    String[] parts = message.split(":");
                    if (parts.length == 3 && parts[0].equals("STATUS_UPDATE")) {
                        String username = parts[1];
                        String status = parts[2];
                        updateUserStatus(username, status);
                    }
                    break;
                case Notification.GENERAL:
                    if (message.equals("REFRESH_CONTACTS")) {
                        refreshContacts();
                    }
                    break;
            }
        });
    }

    private void refreshContacts() {
        if (chatSceneController != null) {
            chatSceneController.refreshContacts();
        }
    }

    @Override
    public boolean acceptRequest(String friend) {
        return controller.acceptRequest(friend);
    }

    @Override
    public void sendMsg(Message message) {

        controller.sendMsg(message);
    }

    /**
     * get received message and pass it to chatSeneController
     *
     * @param message
     */
    @Override
    public void reciveMsg(Message message) {
        try {

            chatSceneController.reciveMsg(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * show error alert
     *
     * @param title
     * @param header
     * @param content
     */
    @Override
    public void showError(String title, String header, String content) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();

    }

    /**
     * alert success operation
     *
     * @param title
     * @param header
     * @param content
     */
    @Override
    public void showSuccess(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public ArrayList<User> getContacts() {
        return controller.getContacts();
    }

    @Override
    public ArrayList<String> checkRequest() {
        return controller.checkRequest();
    }

    public Stage getMainStage() {
        return this.mainStage;
    }

    @Override
    public User getUserInformation() {
        return controller.getUserInformation();
    }

    @Override
    public void receiveAnnouncement(String message) {
        homeBoxController.receiveAnnouncement(message);
    }

    @Override
    public void ignoreRequest(String senderName) {
        controller.ignoreRequest(senderName);
    }

    @Override
    public void saveXMLFile(File file, ArrayList<Message> messages) {
        controller.saveXMLFile(file, messages);
    }

    @Override
    public ClientModelInt getConnection(String Client) {
        return controller.getConnection(Client);
    }

    @Override
    public String getSaveLocation(String sender, String filename) {
        return chatSceneController.getSaveLocation(sender, filename);
    }

    @Override
    public void createGroup(String groupName, ArrayList<String> groupMembers) {
        controller.createGroup(groupName, groupMembers);
    }

    @Override
    public ArrayList<Message> getHistory(String receiver) {
        return controller.getHistory(receiver);
    }

    @Override
    public ArrayList<Pair> getContactsWithType() {
        return controller.getContactsWithType();
    }

    @Override

    public void loadErrorServer() {
        chatSceneController.loadErrorServer();
    }

    @Override
    public void reciveSponser(byte[] data, int dataLength) {
        homeBoxController.reciveSponser(data, dataLength);
    }

    /**
     * Gửi email với subject và nội dung
     * 
     * @param to          Địa chỉ email người nhận
     * @param subject     Chủ đề email
     * @param emailBody   Nội dung email
     * @param senderEmail Email người gửi (không dùng trong API hiện tại nhưng lưu
     *                    lại để tương thích sau này)
     * @return Kết quả gửi email
     */
    @Override
    public boolean sendMail(String to, String subject, String emailBody, String senderEmail) {
        String fullSubject = "Mail From " + getUserInformation().getUsername();
        if (subject != null && !subject.trim().isEmpty()) {
            fullSubject = subject + " - " + fullSubject;
        }
        return controller.sendMail(to, emailBody);
    }

    /**
     * Gửi email với phiên bản đơn giản (tương thích ngược)
     * 
     * @param to        Địa chỉ email người nhận
     * @param emailBody Nội dung email
     * @return Kết quả gửi email
     */
    @Override
    public boolean sendMail(String to, String emailBody) {
        return controller.sendMail(to, emailBody);
    }

    @Override
    public boolean conncetToServer(String host) {
        return controller.conncetToServer(host);
    }

    @Override
    public String getGender(String username) {
        return controller.getGender(username);
    }

    @Override
    public User getUser(String userName) {
        return controller.getUser(userName);
    }

    /**
     * Force refresh of avatar display across all views for a specified user
     * This is called when a user updates their profile
     *
     * @param username the username whose avatar needs refreshing
     */
    public void forceAvatarRefresh(String username) {
        try {
            System.out.println("Force refreshing avatar for: " + username);

            // Step 1: Make sure we have the latest user data from server
            User user = getUser(username);

            // If user doesn't exist or we couldn't fetch info, no point continuing
            if (user == null) {
                System.out.println("Cannot refresh avatar - user info not available");
                return;
            }

            // Step 2: Global UI refresh to ensure new avatar is displayed in all locations
            Platform.runLater(() -> {
                try {
                    // Call comprehensive refresh if available from ChatSceneController
                    if (chatSceneController != null) {
                        // Use the new comprehensive refresh method that targets all UI elements
                        chatSceneController.refreshAllAvatars(username);
                    } else {
                        System.out.println("Chat scene controller not available for avatar refresh");
                    }

                    // Force a slight resize of the main stage to trigger layout refresh
                    Stage stage = getMainStage();
                    if (stage != null) {
                        double width = stage.getWidth();
                        stage.setWidth(width + 0.5);
                        stage.setWidth(width);
                    }

                    System.out.println("Avatar refresh completed successfully");
                } catch (Exception ex) {
                    System.out.println("Error during avatar refresh UI update: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.out.println("Error in forceAvatarRefresh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean updateUser(User user) {
        boolean success = controller.updateUser(user);
        if (success) {
            // After successful update, refresh avatars in all open windows
            forceAvatarRefresh(user.getUsername());
        }
        return success;
    }

    /**
     * Get the ChatSceneController instance
     * 
     * @return ChatSceneController instance
     */
    public ChatSceneController getChatSceneController() {
        return chatSceneController;
    }

    /**
     * Get the status of a friend
     * 
     * @param username The username of the friend
     * @return The status of the friend (online, offline, busy)
     */
    @Override
    public String getStatus(String username) {
        User user = getUser(username);
        if (user != null) {
            return user.getStatus();
        }
        return "offline";
    }

    public void openUpdateUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UpdateUserScene.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resouces/chatBoxStyle.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Update Profile");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showFriendRequest(String message) {
        chatSceneController.notify(message, Notification.FRIEND_REQUSET);
    }

    public void showAcceptFriendRequest(String message) {
        chatSceneController.notify(message, Notification.ACCEPT_FRIEND_REQUEST);
    }

    public void showFriendOnline(String message) {
        chatSceneController.notify(message, Notification.FRIEND_ONLINE);
    }

    public void showFriendOffline(String message) {
        chatSceneController.notify(message, Notification.FRIEND_OFFLINE);
    }

    public void showFriendBusy(String message) {
        chatSceneController.notify(message, Notification.FRIEND_BUSY);
    }

    public void showServerMessage(String message) {
        chatSceneController.notify(message, Notification.SERVER_MESSAGE);
    }

    public void showGeneralNotification(String message) {
        chatSceneController.notify(message, Notification.GENERAL);
    }

    /**
     * Receive notification to update a user's avatar
     * This is called when another user changes their avatar
     * 
     * @param username the username of the user who updated their avatar
     */
    @Override
    public void receiveAvatarUpdate(String username) {
        // Force refresh of avatar display across all views
        forceAvatarRefresh(username);
    }

    /**
     * Send voice message to specific user
     *
     * @throws RemoteException
     */
    public void sendVoiceMessage(VoiceMessage voiceMessage) throws RemoteException {
        try {
            if (voiceMessage.getTo().contains("##")) {
                // Group message handling
                String[] groupMembers = getContacts().stream()
                        .filter(u -> u.getUsername().equals(voiceMessage.getTo()))
                        .map(User::getUsername)
                        .toArray(String[]::new);

                // If no group members found, try getting all contacts
                if (groupMembers.length == 0) {
                    ArrayList<User> allContacts = getContacts();
                    if (allContacts != null) {
                        groupMembers = allContacts.stream()
                                .map(User::getUsername)
                                .toArray(String[]::new);
                    }
                }

                boolean allOffline = true;
                for (String member : groupMembers) {
                    if (!member.equals(getUserInformation().getUsername())) {
                        ClientModelInt connection = getConnection(member);
                        if (connection != null) {
                            try {
                                connection.sendVoiceMessage(member, voiceMessage);
                                allOffline = false;
                            } catch (RemoteException ex) {
                                System.out.println("Could not send voice message to group member: " + member);
                                controller.storeOfflineVoiceMessage(member, voiceMessage);
                            }
                        } else {
                            System.out.println("Group member " + member
                                    + " is offline. Message will be delivered when they come online.");
                            controller.storeOfflineVoiceMessage(member, voiceMessage);
                        }
                    }
                }

                if (allOffline) {
                    System.out.println(
                            "All group members are offline. Messages will be delivered when they come online.");
                }
            } else {
                // Direct message handling
                ClientModelInt connection = getConnection(voiceMessage.getTo());
                if (connection != null) {
                    try {
                        connection.sendVoiceMessage(voiceMessage.getTo(), voiceMessage);
                    } catch (RemoteException ex) {
                        System.out.println(
                                "Error sending voice message to " + voiceMessage.getTo() + ": " + ex.getMessage());
                        controller.storeOfflineVoiceMessage(voiceMessage.getTo(), voiceMessage);
                        throw new RemoteException(
                                "User " + voiceMessage.getTo()
                                        + " is offline. Message stored and will be delivered when they come online.",
                                new Throwable("OFFLINE_HANDLED"));
                    }
                } else {
                    // User is offline - store the message for later delivery
                    System.out.println("User " + voiceMessage.getTo()
                            + " is offline. Voice message will be delivered when they come online.");
                    controller.storeOfflineVoiceMessage(voiceMessage.getTo(), voiceMessage);
                    throw new RemoteException(
                            "User " + voiceMessage.getTo()
                                    + " is offline. Message stored and will be delivered when they come online.",
                            new Throwable("OFFLINE_HANDLED"));
                }
            }
        } catch (RemoteException ex) {
            // Only propagate exceptions that are not our "offline handled" case
            if (ex.getCause() == null || !"OFFLINE_HANDLED".equals(ex.getCause().getMessage())) {
                ex.printStackTrace();
                throw ex;
            }
            // This is our specially marked exception - just rethrow it
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException("Unexpected error sending voice message: " + ex.getMessage());
        }
    }

    /**
     * Receive a voice message from another user
     * 
     * @param voiceMessage
     * @throws IOException
     */
    @Override
    public void receiveVoiceMessage(VoiceMessage voiceMessage) throws IOException {
        // Forward to chat box controller if available
        if (chatBoxController != null) {
            chatBoxController.receiveVoiceMessage(voiceMessage);
        } else {
            // If no chat box controller is available, show a notification
            notify("New voice message from " + voiceMessage.getFrom(), Notification.GENERAL);
        }
    }

    public void updateUserStatus(String username, String status) {
        // Chỉ cập nhật UI nếu đây là status của người dùng khác
        User currentUser = getUserInformation();
        if (currentUser != null && !username.equals(currentUser.getUsername())) {
            // Update status in the UI immediately
            Platform.runLater(() -> {
                // Update status in chat scene if it's open
                if (chatSceneController != null) {
                    chatSceneController.updateContactStatus(username, status);
                }

                // Update status in any open chat boxes
                if (tabsControllers != null) {
                    for (ChatBoxController controller : tabsControllers.values()) {
                        if (controller != null) {
                            controller.updateFriendStatus(username, status);
                        }
                    }
                }
            });
        } else if (currentUser != null && username.equals(currentUser.getUsername())) {
            // Đây là status của người dùng hiện tại, cập nhật dropdown status
            Platform.runLater(() -> {
                if (chatSceneController != null) {
                    chatSceneController.updateStatusDropdown(status);
                }
            });
        }
    }

    public void addChatBoxController(String username, ChatBoxController controller) {
        if (tabsControllers != null) {
            tabsControllers.put(username, controller);
        }
    }

    public void removeChatBoxController(String username) {
        if (tabsControllers != null) {
            tabsControllers.remove(username);
        }
    }

    public ChatBoxController getChatBoxController(String username) {
        return tabsControllers != null ? tabsControllers.get(username) : null;
    }
}
