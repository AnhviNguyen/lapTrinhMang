package model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ClientModelInt extends Remote {

    void notify(String message, int type) throws RemoteException;

    // void reciveMsg(String msg) throws RemoteException;
    void reciveMsg(Message message) throws RemoteException;

    /*
     * receive Announcement from server
     *
     * @param message
     * 
     * @throws java.rmi.RemoteException
     */
    void receiveAnnouncement(String message) throws RemoteException;

    /**
     *
     * @param sender
     * @param filename
     * @return url location or null if not file choosen
     * @throws RemoteException
     */
    String getSaveLocation(String sender, String filename) throws RemoteException;

    /**
     * save file
     *
     * @param path
     * @param filename
     * @param append
     * @param data
     * @param dataLength
     * @throws RemoteException
     */
    void reciveFile(String path, String filename, boolean append, byte[] data, int dataLength) throws RemoteException;

    /**
     *
     * @param data
     * @param dataLength
     */
    void reciveSponser(byte[] data, int dataLength) throws RemoteException;

    /**
     * check to online or not by server
     *
     * @return true if is active
     * @throws RemoteException
     */
    boolean isOnline() throws RemoteException;

    void loadErrorServer() throws RemoteException;

    /**
     * Receive notification that a user has updated their avatar
     * This will trigger the client to refresh the avatar for that user
     *
     * @param username The username of the user who updated their avatar
     * @throws RemoteException
     */
    void receiveAvatarUpdate(String username) throws RemoteException;

    /**
     * Receive a voice message from another user
     *
     * @param voiceMessage The voice message to receive
     * @throws RemoteException If there is an error during remote communication
     */
    void receiveVoiceMessage(VoiceMessage voiceMessage) throws RemoteException;

    /**
     * Receive voice call request
     * 
     * @param caller The username of the caller
     * @throws RemoteException
     */
    void receiveVoiceCallRequest(String caller) throws RemoteException;

    /**
     * Receive voice call acceptance
     *
     * @param receiver The username of the receiver
     * @throws RemoteException
     */
    void receiveVoiceCallAcceptance(String receiver) throws RemoteException;

    /**
     * Receive voice call rejection
     *
     * @param receiver The username of the receiver
     * @throws RemoteException
     */
    void receiveVoiceCallRejection(String receiver) throws RemoteException;

    /**
     * Receive voice call end notification
     *
     * @param otherUser The username of the other user
     * @throws RemoteException
     */
    void receiveVoiceCallEnd(String otherUser) throws RemoteException;

    /**
     * Receive voice data during a call
     * 
     * @param sender    The username of the sender
     * @param voiceData The voice data
     * @throws RemoteException
     */
    void receiveVoiceData(String sender, byte[] voiceData) throws RemoteException;

    /**
     * Receive audio data during a voice call
     * 
     * @param sender     The username of the sender
     * @param audioData  The audio data bytes
     * @param dataLength The length of valid data in the audioData array
     * @throws RemoteException
     */
    void receiveAudioData(String sender, byte[] audioData, int dataLength) throws RemoteException;

    /**
     * Handle voice call related messages (requests, accepted, rejected, ended)
     * 
     * @param message The message containing call information
     * @throws RemoteException
     */
    void handleCallMessage(Message message) throws RemoteException;
}
