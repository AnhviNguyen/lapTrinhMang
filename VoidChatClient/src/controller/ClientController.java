package controller;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import model.ClientModel;
import model.ClientModelInt;
import model.ClientPrivateModel;
import model.Message;
import model.ServerModelInt;
import model.User;
import utilitez.Notification;
import utilitez.Pair;
import view.ClientView;
import model.VoiceMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientController implements ClientControllerInt {

    private ClientView view;
    private ClientModel model;
    private ClientPrivateModel pmodel;
    private ServerModelInt serverModelInt;
    private User loginUser;
    private Thread checkServerStatus;
    private static ClientController instance;

    // Store offline voice messages until recipients come online
    private Map<String, List<VoiceMessage>> offlineVoiceMessages = new ConcurrentHashMap<>();

    public ClientController(ClientView view) {
        try {
            // connect with view
            this.view = view;
            instance = this;

            // connect with model
            model = new ClientModel(this);

            // connect to private model
            pmodel = new ClientPrivateModel(this);

        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get the singleton instance of ClientController
     * 
     * @return ClientController instance
     */
    public static ClientController getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        Application.launch(ClientView.class, args);

    }

    @Override
    public boolean conncetToServer(String host) {
        try {
            System.out.println("Attempting to connect to server at: " + host);

            // Create the registry with the specified host and port
            Registry reg = LocateRegistry.getRegistry(host, 1050);

            // Look up the server object
            serverModelInt = (ServerModelInt) reg.lookup("voidChatServer");

            System.out.println("Successfully connected to server");
            return true;
        } catch (RemoteException ex) {
            System.err.println("Remote exception while connecting to server: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (NotBoundException ex) {
            System.err.println("Server service 'voidChatServer' not bound: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.err.println("Unexpected error connecting to server: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean signup(User user) throws Exception {

        try {
            return serverModelInt.signup(user);
        } catch (RemoteException | NullPointerException ex) {
            ex.printStackTrace();
            throw new Exception("Server not working now");
        }

    }

    @Override
    public User signin(String username, String password) throws Exception {

        try {
            // assigne data return to loginUser
            loginUser = serverModelInt.signin(username, password);
            // register client to server
            if (loginUser != null) {
                registerToServer(loginUser.getUsername(), model);
            }
            // check server status
            checkServerStatus = new Thread(() -> {
                while (true)
                    checkServerStatus();
            });
            checkServerStatus.start();

        } catch (RemoteException | NullPointerException ex) {
            ex.printStackTrace();
            throw new Exception("Server not working now");
        }
        return loginUser; // return null if faild

    }

    @Override
    public void registerToServer(String username, ClientModelInt obj) throws Exception {
        try {
            if (!serverModelInt.register(username, obj)) {
                throw new RuntimeException("User already Login");
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ArrayList<User> getContacts() {
        try {
            return serverModelInt.getContacts(loginUser.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    @Override
    public ArrayList<String> checkRequest() {
        try {
            return serverModelInt.checkRequest(loginUser.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    ////////////////////////////////////////////////////////////

    @Override
    public void changeStatus(String status) {
        try {
            serverModelInt.changeStatus(loginUser.getUsername(), status);

            // If the user is coming online, check for offline messages to deliver to others
            if (status.equals("online")) {
                // Scan through contacts to see who's online and deliver any pending messages
                ArrayList<User> contacts = getContacts();
                if (contacts != null) {
                    for (User contact : contacts) {
                        if (contact.getStatus() != null && contact.getStatus().equals("online")) {
                            // This contact is online - see if we have messages for them
                            if (offlineVoiceMessages.containsKey(contact.getUsername())) {
                                checkAndDeliverOfflineVoiceMessages(contact.getUsername());
                            }
                        }
                    }
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void logout() {
        try {
            serverModelInt.unregister(loginUser.getUsername());
            checkServerStatus.interrupt();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int sendRequest(String reciverName, String category) {
        try {
            return serverModelInt.sendRequest(loginUser.getUsername(), reciverName, category);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return 0;
        }

    }

    @Override
    public void notify(String message, int type) {
        view.notify(message, type);
    }

    @Override
    public boolean acceptRequest(String friendName) {
        try {
            return serverModelInt.acceptRequest(friendName, loginUser.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void sendMsg(Message message) {
        try {
            serverModelInt.sendMsg(message);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reciveMsg(Message message) {
        view.reciveMsg(message);
    }

    @Override
    public User getUserInformation() {
        return this.loginUser;
    }

    @Override
    public void receiveAnnouncement(String message) {
        view.receiveAnnouncement(message);
        view.notify("New Message from Server Open Home to See it", Notification.SERVER_MESSAGE);
    }

    public void ignoreRequest(String senderName) {
        try {
            serverModelInt.ignoreRequest(senderName, loginUser.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveXMLFile(File file, ArrayList<Message> messages) {
        pmodel.saveXMLFile(file, messages, loginUser);
    }

    @Override
    public ClientModelInt getConnection(String Client) {
        try {
            return serverModelInt.getConnection(Client);
        } catch (RemoteException ex) {
            Logger.getLogger(ClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getSaveLocation(String sender, String filename) {
        return view.getSaveLocation(sender, filename);
    }

    @Override
    public void createGroup(String groupName, ArrayList<String> groupMembers) {
        try {
            serverModelInt.createGroup(groupName, groupMembers);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public ArrayList<Message> getHistory(String receiver) {
        try {
            return serverModelInt.getHistory(loginUser.getUsername(), receiver);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Pair> getContactsWithType() {
        try {
            return serverModelInt.getContactsWithType(loginUser.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void loadErrorServer() {
        view.loadErrorServer();
    }

    @Override
    public void reciveSponser(byte[] data, int dataLength) {
        view.reciveSponser(data, dataLength);
    }

    @Override
    public boolean sendMail(String to, String emailBody) {
        try {
            return serverModelInt.sendMail(to, " Mail From " + loginUser.getUsername(), emailBody);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void checkServerStatus() {
        try {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (serverModelInt != null) {
                        Platform.runLater(() -> {
                            if (view != null && view.serverStatusLabel != null) {
                                view.serverStatusLabel.setText("Server Status: Online");
                                view.serverStatusLabel.setTextFill(Color.GREEN);
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            if (view != null && view.serverStatusLabel != null) {
                                view.serverStatusLabel.setText("Server Status: Offline");
                                view.serverStatusLabel.setTextFill(Color.RED);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    // Log the interruption and break the loop
                    System.out.println("Server status check interrupted: " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getGender(String username) {
        try {
            return serverModelInt.getGender(username);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public User getUser(String userName) {
        try {
            return serverModelInt.getUser(userName);
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            boolean success = serverModelInt.updateUser(user);
            if (success) {
                // Update the local loginUser object with new information
                this.loginUser = user;
            }
            return success;
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Handles notification that a user has updated their avatar
     * Forwards to the view for UI updates
     * 
     * @param username username of the user who updated their avatar
     */
    @Override
    public void receiveAvatarUpdate(String username) {
        try {
            System.out.println("Controller received avatar update notification for: " + username);

            // Check if this is the current user updating their own avatar
            User currentUser = getUserInformation();
            boolean isCurrentUser = (currentUser != null &&
                    currentUser.getUsername() != null &&
                    currentUser.getUsername().equals(username));

            if (isCurrentUser) {
                System.out.println("Current user updated their avatar - refreshing UI");
                // Make sure we get a fresh user record
                serverModelInt.refreshUser(currentUser.getUsername());
            }

            // Forward to the view
            view.receiveAvatarUpdate(username);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Send a voice message to another user
     *
     * @param receiver     The username of the receiver
     * @param voiceMessage The voice message to send
     */
    public void sendVoiceMessage(String receiver, VoiceMessage voiceMessage) {
        try {
            serverModelInt.sendVoiceMessage(loginUser.getUsername(), receiver, voiceMessage);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Receive a voice message from another user
     *
     * @param voiceMessage The voice message that was received
     */
    public void receiveVoiceMessage(VoiceMessage voiceMessage) {
        try {
            view.receiveVoiceMessage(voiceMessage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Store a voice message for an offline recipient to be delivered when they come
     * online
     * 
     * @param receiver     The username of the offline recipient
     * @param voiceMessage The voice message to be stored
     */
    public void storeOfflineVoiceMessage(String receiver, VoiceMessage voiceMessage) {
        // Create a list for this user if it doesn't exist yet
        offlineVoiceMessages.computeIfAbsent(receiver, k -> new CopyOnWriteArrayList<>());

        // Add the message to the list
        offlineVoiceMessages.get(receiver).add(voiceMessage);

        System.out.println("Stored offline voice message for " + receiver + ". Total pending messages: "
                + offlineVoiceMessages.get(receiver).size());
    }

    /**
     * Check if there are pending offline voice messages for a user who just came
     * online
     * and attempt to deliver them
     * 
     * @param username The username of the user who came online
     */
    public void checkAndDeliverOfflineVoiceMessages(String username) {
        List<VoiceMessage> messages = offlineVoiceMessages.get(username);
        if (messages != null && !messages.isEmpty()) {
            System.out.println("Found " + messages.size() + " offline voice messages for " + username);

            // Get a connection to the user
            ClientModelInt connection = getConnection(username);
            if (connection != null) {
                List<VoiceMessage> failedMessages = new ArrayList<>();

                // Try to send each message
                for (VoiceMessage message : messages) {
                    try {
                        connection.sendVoiceMessage(username, message);
                        System.out.println("Successfully delivered offline voice message to " + username);
                    } catch (RemoteException ex) {
                        System.out.println(
                                "Failed to deliver offline voice message to " + username + ": " + ex.getMessage());
                        failedMessages.add(message);
                    }
                }

                // Clear successfully sent messages and keep failed ones
                messages.clear();
                if (!failedMessages.isEmpty()) {
                    messages.addAll(failedMessages);
                } else {
                    // Remove the entry if all messages were delivered
                    offlineVoiceMessages.remove(username);
                }
            }
        }
    }

}
