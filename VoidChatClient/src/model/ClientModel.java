package model;

import controller.ClientController;
import view.ChatBoxController;
import view.ChatSceneController;
import view.ClientView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javafx.application.Platform;

/**
 *
 * @author MotYim
 */
public class ClientModel extends UnicastRemoteObject implements ClientModelInt, Serializable {

    private ClientController controller;

    public ClientModel(ClientController controller) throws RemoteException {
        this.controller = controller;
    }

    @Override
    ////////////////////////// 3adlt
    ////////////////////////// hna//////////////////////////////////////////////////
    public void notify(String message, int type) {
        controller.notify(message, type);
    }

    @Override
    public void reciveMsg(Message message) {
        controller.reciveMsg(message);
    }

    @Override
    public void receiveAnnouncement(String message) {
        controller.receiveAnnouncement(message);
    }

    @Override
    public String getSaveLocation(String sender, String filename) {
        return controller.getSaveLocation(sender, filename);
    }

    @Override
    public void reciveFile(String path, String filename, boolean append, byte[] data, int dataLength) {

        try {
            File f;

            String[] split = path.split("\\.(?=[^\\.]+$)");
            // handle not write extesion
            if (split.length < 2) {
                split = filename.split("\\.(?=[^\\.]+$)");
                String extension = "." + split[1];
                f = new File(path + extension);
            } else {
                f = new File(path);
            }

            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f, append);
            out.write(data, 0, dataLength);
            out.flush();
            out.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void reciveSponser(byte[] data, int dataLength) throws RemoteException {
        controller.reciveSponser(data, dataLength);
    }

    @Override
    public boolean isOnline() throws RemoteException {
        return true;
    }

    public void loadErrorServer() throws RemoteException {
        controller.loadErrorServer();
    }

    /**
     * Receive notification that a user has updated their avatar
     * This will trigger the client to refresh the avatar for that user
     * 
     * @param username The username of the user who updated their avatar
     * @throws RemoteException
     */
    @Override
    public void receiveAvatarUpdate(String username) throws RemoteException {
        try {
            System.out.println("Received avatar update notification for user: " + username);

            // Get the ChatSceneController instance
            ChatSceneController chatController = ClientView.getInstance().getChatSceneController();
            if (chatController != null) {
                // Handle the avatar update
                chatController.handleAvatarUpdate(username);

                // Force refresh of all UI elements showing this user's avatar
                Platform.runLater(() -> {
                    try {
                        // Refresh contacts list
                        chatController.refreshContacts();

                        // Refresh any open chat windows
                        if (chatController.getTabsControllers() != null) {
                            for (ChatBoxController chatBoxController : chatController.getTabsControllers().values()) {
                                chatBoxController.refreshUserAvatar(username);
                            }
                        }

                        // Refresh tab icons
                        chatController.refreshTabAvatar(username);

                        System.out.println("Successfully processed avatar update for user: " + username);
                    } catch (Exception ex) {
                        System.out.println("Error processing avatar update in UI: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            } else {
                System.out.println("ChatSceneController not available for avatar update");
            }
        } catch (Exception ex) {
            System.out.println("Error handling avatar update notification: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Send a voice message to another user
     * 
     * @param receiver     The username of the receiver
     * @param voiceMessage The voice message to send
     * @throws RemoteException
     */
    @Override
    public void sendVoiceMessage(String receiver, VoiceMessage voiceMessage) throws RemoteException {
        // Forward to controller to handle the sending logic
        controller.sendVoiceMessage(receiver, voiceMessage);
    }

    /**
     * Receive a voice message from another user
     * 
     * @param voiceMessage The voice message that was received
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceMessage(VoiceMessage voiceMessage) throws RemoteException {
        // Forward the received voice message to controller
        controller.receiveVoiceMessage(voiceMessage);
    }

    /**
     * Receive audio data during a voice call
     * 
     * @param sender     The username of the sender
     * @param audioData  The audio data bytes
     * @param dataLength The length of valid data in the audioData array
     * @throws RemoteException
     */
    @Override
    public void receiveAudioData(String sender, byte[] audioData, int dataLength) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                clientView.chatBoxController.receiveAudioData(sender, audioData, dataLength);
            });
        }
    }

    /**
     * Handle voice call related messages (requests, accepted, rejected, ended)
     * 
     * @param message The message containing call information
     * @throws RemoteException
     */
    @Override
    public void handleCallMessage(Message message) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null) {
            String messageType = message.getType();
            String sender = message.getFrom();

            Platform.runLater(() -> {
                if (clientView.chatBoxController != null) {
                    if (messageType.equals("voice-call-request")) {
                        clientView.chatBoxController.receiveCallRequest(sender);
                    } else if (messageType.equals("voice-call-accepted")) {
                        clientView.chatBoxController.handleCallAccepted(sender);
                    } else if (messageType.equals("voice-call-rejected")) {
                        clientView.chatBoxController.handleCallRejected(sender);
                    } else if (messageType.equals("voice-call-end")) {
                        clientView.chatBoxController.handleCallEnded(sender);
                    }
                }
            });
        }
    }

    /**
     * Receive voice call request
     * 
     * @param caller The username of the caller
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceCallRequest(String caller) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                clientView.chatBoxController.receiveCallRequest(caller);
            });
        }
    }

    /**
     * Receive voice call acceptance
     *
     * @param receiver The username of the receiver
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceCallAcceptance(String receiver) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                clientView.chatBoxController.handleCallAccepted(receiver);
            });
        }
    }

    /**
     * Receive voice call rejection
     *
     * @param receiver The username of the receiver
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceCallRejection(String receiver) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                clientView.chatBoxController.handleCallRejected(receiver);
            });
        }
    }

    /**
     * Receive voice call end notification
     *
     * @param otherUser The username of the other user
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceCallEnd(String otherUser) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                clientView.chatBoxController.handleCallEnded(otherUser);
            });
        }
    }

    /**
     * Receive voice data during a call
     * 
     * @param sender    The username of the sender
     * @param voiceData The voice data
     * @throws RemoteException
     */
    @Override
    public void receiveVoiceData(String sender, byte[] voiceData) throws RemoteException {
        ClientView clientView = ClientView.getInstance();
        if (clientView != null && clientView.chatBoxController != null) {
            Platform.runLater(() -> {
                // This should be implemented if you have a method for handling voice data
                // differently than audio data
                // For now, we'll use receiveAudioData as they serve similar purposes
                clientView.chatBoxController.receiveAudioData(sender, voiceData, voiceData.length);
            });
        }
    }
}
