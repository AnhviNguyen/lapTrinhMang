package view;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import model.ClientModelInt;
import model.Message;
import model.User;
import model.VoiceMessage;
import utilitez.Pair;

/**
 *
 * @author MotYim
 */
public interface ClientViewInt {

    /**
     * connect to server by host ip
     * 
     * @param host
     * @return
     */
    public boolean conncetToServer(String host);

    boolean signup(User user) throws Exception;

    User signin(String username, String password) throws Exception;

    void changeStatus(String status);

    void logout();

    int sendRequest(String friend, String category);

    void notify(String message, int type);

    boolean acceptRequest(String friend);

    // void sendMsg(String friendName,String message);
    void sendMsg(Message message);

    // void reciveMsg(String msg);
    void reciveMsg(Message message);

    ArrayList<User> getContacts();

    public void showError(String title, String header, String content);

    public void showSuccess(String title, String header, String content);

    ArrayList<String> checkRequest();

    /**
     * get login user info
     * 
     * @return Login User
     */
    User getUserInformation();

    /**
     * receive Announcement from server
     *
     * @param message
     */
    void receiveAnnouncement(String message);

    /**
     * refuser friend request
     *
     * @param senderName
     */
    public void ignoreRequest(String senderName);

    /**
     * save messages on XML format on file
     *
     * @param file
     * @param messages
     */
    public void saveXMLFile(File file, ArrayList<Message> messages);

    /**
     * make peet-to-peer connection with Client
     *
     * @param Client
     * @return connection
     */
    ClientModelInt getConnection(String Client);

    /**
     *
     * @param sender
     * @param filename
     * @return url location or null if not file choosen
     */
    String getSaveLocation(String sender, String filename);

    void createGroup(String groupName, ArrayList<String> groupMembers);

    ArrayList<Message> getHistory(String receiver);

    ArrayList<Pair> getContactsWithType();

    void loadErrorServer();

    void reciveSponser(byte[] data, int dataLength);

    /**
     * Gửi email
     * 
     * @param to        Địa chỉ email người nhận
     * @param emailBody Nội dung email
     * @return true nếu thành công
     */
    boolean sendMail(String to, String emailBody);

    /**
     * Gửi email với đầy đủ thông tin
     * 
     * @param to          Địa chỉ email người nhận
     * @param subject     Chủ đề email
     * @param emailBody   Nội dung email
     * @param senderEmail Email người gửi
     * @return kết quả gửi email
     */
    boolean sendMail(String to, String subject, String emailBody, String senderEmail);

    String getGender(String username);

    User getUser(String userName);

    /**
     * Update user information
     * 
     * @param user The updated user object
     * @return true if update successful, false otherwise
     */
    boolean updateUser(User user);

    /**
     * Get the status of a friend
     * 
     * @param username The username of the friend
     * @return The status of the friend (online, offline, busy)
     */
    String getStatus(String username);

    /**
     * Receive notification to update a user's avatar
     * This is called when another user changes their avatar
     * 
     * @param username the username of the user who updated their avatar
     */
    void receiveAvatarUpdate(String username);

    /**
     * Receive voice message from another user
     * 
     * @param voiceMessage The voice message received
     * @throws IOException
     */
    void receiveVoiceMessage(VoiceMessage voiceMessage) throws IOException;

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
