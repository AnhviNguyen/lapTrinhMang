package model;

import controller.ServerController;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import utilitez.Constant;
import utilitez.Notification;
import utilitez.Pair;

import static utilitez.Notification.ACCEPT_FRIEND_REQUEST;

/**
 *
 * @author Roma
 */
public class ServerModel extends UnicastRemoteObject implements ServerModelInt {

    @Override
    public ArrayList<String> getGroups(String username) throws RemoteException {
        return null;
    }

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String query;
    private String property = System.getProperty("user.dir");
    private ServerController controller;
    private boolean isClosed; // check if databased is closed
    private PreparedStatement preparedStatement;

    public ServerModel(ServerController controller) throws RemoteException {
        this.controller = controller;
    }

    /**
     * connect to database
     */
    private void getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Driver cho MySQL
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/voidchat", // URL kết nối tới database trong XAMPP
                    "root", // Username mặc định của XAMPP
                    "" // Password mặc định là rỗng trong XAMPP
            );
            isClosed = false;
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * close connection to database
     */
    private void closeResources() {
        try {
            if (!isClosed) {
                // resultSet.close();
                statement.close();
                connection.close();
                isClosed = true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized boolean signup(User user) throws RemoteException {
        try {
            getConnection();
            query = "insert into UserTable (username,email,fname,lname,password,gender,country,status,image) values ('"
                    + user.getUsername() + "','" + user.getEmail() + "','" + user.getFname() + "','" + user.getLname()
                    + "','" + user.getPassword() + "','" + user.getGender() + "','" + user.getCountry() + "','"
                    + user.getStatus() + "','" + user.getImage() + "')";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public synchronized User signin(String username, String password) {
        try {
            getConnection();
            query = "select * from UserTable where username='" + username + "' and password='" + password + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFname(resultSet.getString("fname"));
                user.setLname(resultSet.getString("lname"));
                user.setPassword(resultSet.getString("password"));
                user.setGender(resultSet.getString("gender"));
                user.setCountry(resultSet.getString("country"));
                user.setStatus(resultSet.getString("status"));
                user.setImage(resultSet.getString("image"));
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return null;
    }

    @Override
    public synchronized boolean register(String username, ClientModelInt obj) throws RemoteException {
        return controller.register(username, obj);
    }

    @Override
    public synchronized void unregister(String username) throws RemoteException {
        controller.unregister(username);
    }

    @Override
    public synchronized ArrayList<String> checkRequest(String username) throws RemoteException {
        ArrayList<String> friendsNames = null;
        try {
            getConnection();
            query = "select sender from Requests where receiver = '" + username + "' And type <> 'Block'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                friendsNames = new ArrayList<>();
                friendsNames.add(resultSet.getString("sender"));
                while (resultSet.next()) {
                    friendsNames.add(resultSet.getString("sender"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return friendsNames;
    }

    @Override
    public synchronized boolean acceptRequest(String senderName, String reciverName) throws RemoteException {
        try {
            String type = null;
            getConnection();
            query = "select type from Requests where receiver='" + reciverName + "' and sender='" + senderName + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                type = resultSet.getString("type");
            }

            // Insert into relationship table
            query = "insert into relationship (user, friend, type) values ('" + reciverName + "','" + senderName + "','"
                    + type + "')";
            statement.executeUpdate(query);

            // Also insert the reverse relationship
            query = "insert into relationship (user, friend, type) values ('" + senderName + "','" + reciverName + "','"
                    + type + "')";
            statement.executeUpdate(query);

            // Delete from Requests table
            query = "delete from Requests where sender='" + senderName + "' and receiver='" + reciverName + "'";
            statement.executeUpdate(query);

            // Notify that friend accepted friendship
            notify(senderName, reciverName + " Accept Your Friend Request", ACCEPT_FRIEND_REQUEST);

            // Notify both users to refresh their contact lists
            notify(senderName, "REFRESH_CONTACTS", Notification.GENERAL);
            notify(reciverName, "REFRESH_CONTACTS", Notification.GENERAL);

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public synchronized void notify(String reciver, String message, int type) throws RemoteException {
        controller.notify(reciver, message, type);
    }

    @Override
    public synchronized void changeStatus(String username, String status) throws RemoteException {
        try {
            getConnection();
            // Get current status before updating
            String currentStatus = null;
            query = "select status from UserTable where username='" + username + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                currentStatus = resultSet.getString("status");
            }
            statement.close();

            // Only proceed if status is actually changing
            if (currentStatus == null || !status.equals(currentStatus)) {
                // Update status in database
                query = "update UserTable set status=? where username=?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, status);
                preparedStatement.setString(2, username);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                // Get all friends of the user
                ArrayList<User> userFriends = getContacts(username);
                if (userFriends != null) {
                    for (User friend : userFriends) {
                        // Get the friend's client connection if they are online
                        ClientModelInt friendClient = controller.getOnlineUsers().get(friend.getUsername());
                        if (friendClient != null) {
                            try {
                                // Send immediate status notification only if status changed
                                switch (status.toLowerCase()) {
                                    case "online":
                                        friendClient.notify(username + " Become online ", Notification.FRIEND_ONLINE);
                                        break;
                                    case "offline":
                                        friendClient.notify(username + " Become offline ", Notification.FRIEND_OFFLINE);
                                        break;
                                    case "busy":
                                        friendClient.notify(username + " Become busy ", Notification.FRIEND_BUSY);
                                        break;
                                }

                                // Force a refresh of the friend's contact list
                                friendClient.notify("REFRESH_CONTACTS", Notification.GENERAL);
                            } catch (RemoteException ex) {
                                System.out.println("Failed to notify " + friend.getUsername() + " about status change: "
                                        + ex.getMessage());
                                // Remove disconnected client
                                controller.unregister(friend.getUsername());
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeResources();
        }
    }

    @Override
    public synchronized void sendMsg(Message message) {
        if (controller == null) {
            System.err.println("Error: ServerController is null");
            return;
        }

        try {
            if (message.getTo().startsWith("##")) {
                // This is a group message
                // Let the controller handle group messages with in-memory data
                controller.recieveMsg(message);
            } else {
                // Regular direct message
                insertMessage(message);
                controller.recieveMsg(message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized ArrayList<User> getContacts(String userName) {
        ArrayList<User> contacts = new ArrayList<User>();
        try {
            getConnection();
            query = "select u.* from UserTable u inner join relationship r on u.username = r.friend where r.user='"
                    + userName + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFname(resultSet.getString("fname"));
                user.setLname(resultSet.getString("lname"));
                user.setPassword(resultSet.getString("password"));
                user.setGender(resultSet.getString("gender"));
                user.setCountry(resultSet.getString("country"));
                user.setStatus(resultSet.getString("status"));
                user.setImage(resultSet.getString("image"));
                contacts.add(user);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return contacts.size() == 0 ? null : contacts;
    }

    @Override
    public synchronized int sendRequest(String senderName, String reciverName, String type) throws RemoteException {
        if (senderName.equals(reciverName)) {
            return Constant.SAME_NAME;
        }
        try {
            getConnection();
            query = "select * from UserTable where username='" + reciverName + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (!(resultSet.next())) {

                return Constant.USER_NOT_EXIST;
            }

            query = "select * from Relationship where (user='" + senderName + "' and friend='" + reciverName + "') or "
                    + "(user='" + reciverName + "' and friend='" + senderName + "')";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {

                return Constant.ALREADY_FRIENDS;
            }

            query = "select * from Requests where (sender='" + senderName + "' and receiver='" + reciverName + "')or"
                    + "(sender='" + reciverName + "' and receiver='" + senderName + "')";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {

                return Constant.REQUEST_ALREADY_EXIST;
            }

            query = "insert into Requests (sender,receiver,type)values ('" + senderName + "','" + reciverName + "','"
                    + type + "')";
            statement.executeUpdate(query);

            // zwat hna
            if (!type.equals("Block"))
                notify(reciverName, senderName + " Want to be your Friend", Notification.FRIEND_REQUSET); // notify if
                                                                                                          // sccuess
                                                                                                          // only

            return Constant.SENDED;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return Constant.EXCEPTION;
        } finally {
            closeResources();
        }

    }

    @Override
    public synchronized void ignoreRequest(String senderName, String reciverName) {
        try {
            getConnection();
            query = "delete from Requests where sender='" + senderName + "' and receiver='" + reciverName + "'";
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
    }

    public synchronized ArrayList<Integer> getStatistics() {
        int countUsers = 0;
        ArrayList<Integer> users = new ArrayList<Integer>();
        try {
            getConnection();
            query = "select * from UserTable where status='online'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                countUsers++;
            }
            users.add(countUsers);
            countUsers = 0;

            query = "select * from UserTable where status='offline'";
            ResultSet resultSet2 = statement.executeQuery(query);
            while (resultSet2.next()) {
                countUsers++;

            }
            users.add(countUsers);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return users;
    }

    public synchronized ArrayList<Pair> getGender() {
        int count = 0;
        ArrayList<Pair> users = new ArrayList<Pair>();
        Pair user = new Pair();
        try {
            getConnection();
            query = "select * from UserTable where gender='Female'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                count++;
            }
            user = new Pair("Female", count);
            users.add(user);
            count = 0;
            user = new Pair();
            query = "select * from UserTable where gender='Male'";
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                count++;
            }
            user = new Pair("Male", count);
            users.add(user);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return users;
    }

    public synchronized ArrayList<Pair> getCountries() {
        int count = 0;
        String country = null;
        ArrayList<String> distinctCountries = new ArrayList<String>();
        ArrayList<Pair> countriesPairs = new ArrayList<Pair>();
        Pair myPair = new Pair();

        try {
            getConnection();
            query = "select distinct country from UserTable";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                distinctCountries.add(resultSet.getString("country"));
            }
            for (int i = 0; i < distinctCountries.size(); i++) {
                query = "select * from UserTable where country='" + distinctCountries.get(i) + "'";
                resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    count++;
                    country = resultSet.getString("country");
                }
                myPair = new Pair(country, count);
                countriesPairs.add(myPair);
                count = 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return countriesPairs;

    }

    @Override
    public synchronized ClientModelInt getConnection(String Client) {
        return controller.getConnection(Client);
    }

    @Override
    public synchronized void createGroup(String groupName, ArrayList<String> groupMembers) {
        try {
            // Instead of database operations, just forward to controller
            controller.createGroup(groupName, groupMembers);

            // Notify each member about the new group
            for (String member : groupMembers) {
                notify(member, "You've been added to group: " + groupName, Notification.GENERAL);
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void insertMessage(Message message) {
        try {
            getConnection();
            query = "insert into Message(fontSize,`from`,`to`,date,fontColor,fontFamily,fontStyle,body,fontWeight,underLine)values ("
                    + message.getFontsSize() + ",'" + message.getFrom() + "','"
                    + message.getTo() + "','" + message.getDate() + "','" + message.getFontColor() + "','"
                    + message.getFontFamily() + "','"
                    + message.getFontStyle() + "','" + message.getBody() + "','" + message.getFontWeight() + "','"
                    + message.getUnderline().toString() + "')";

            statement = connection.createStatement();
            statement.executeUpdate(query);
            closeResources();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    public synchronized ArrayList<Message> getHistory(String sender, String receiver) {
        ArrayList<Message> messages = new ArrayList<Message>();
        try {
            getConnection();
            query = "select * from Message where (`from` = '" + sender + "' and `to`='" + receiver + "') or"
                    + " (`to`='" + sender + "'and `from`='" + receiver + "')";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                int fontSize = resultSet.getInt(2);
                String from = resultSet.getString(3);
                String to = resultSet.getString(4);
                String date = resultSet.getString(5);
                String fontColor = resultSet.getString(6);
                String fontFamily = resultSet.getString(7);
                String fontStyle = resultSet.getString(8);
                String body = resultSet.getString(9);
                String fontWeight = resultSet.getString(10);
                boolean underline = Boolean.parseBoolean(resultSet.getString(11));

                String format = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
                GregorianCalendar cal = new GregorianCalendar();
                try {
                    cal.setTime(new SimpleDateFormat(format).parse(date));
                    XMLGregorianCalendar calendar;
                    try {
                        calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                        Message message = new Message(fontSize, from, to, calendar, fontColor, fontFamily, fontStyle,
                                body, fontWeight, underline);
                        messages.add(message);
                    } catch (DatatypeConfigurationException ex) {
                        ex.printStackTrace();
                    }

                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return messages.size() == 0 ? null : messages;
    }

    @Override
    public synchronized ArrayList<Pair> getContactsWithType(String userName) {
        ArrayList<Pair> contacts = new ArrayList<>();
        try {
            getConnection();
            query = "select u.*, r.type from UserTable u inner join relationship r on u.username = r.friend where r.user='"
                    + userName + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFname(resultSet.getString("fname"));
                user.setLname(resultSet.getString("lname"));
                user.setPassword(resultSet.getString("password"));
                user.setGender(resultSet.getString("gender"));
                user.setCountry(resultSet.getString("country"));
                user.setStatus(resultSet.getString("status"));
                user.setImage(resultSet.getString("image"));
                contacts.add(new Pair(user, resultSet.getString("type")));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return contacts;
    }

    @Override
    public synchronized boolean sendMail(String to, String subject, String emailBody) throws RemoteException {
        return controller.sendMail(to, subject, emailBody);
    }

    public synchronized ArrayList<User> getAllUsers() {
        ArrayList<User> users = new ArrayList<User>();
        try {
            getConnection();
            query = "select * from UserTable";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String fname = resultSet.getString("fname");
                String lname = resultSet.getString("lname");
                String password = "";
                String gender = resultSet.getString("gender");
                String status = resultSet.getString("status");
                String country = resultSet.getString("country");
                User user = new User(username, email, fname, lname, password, gender, country, status);
                users.add(user);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return users.size() == 0 ? null : users;
    }

    public synchronized void GenerateUserFX(User user) {
        UserFx userFx = new UserFx(user.getUsername(), user.getEmail(), user.getFname(), user.getLname(),
                user.getGender(), user.getCountry());

        controller.GenerateUserFX(userFx);
    }

    // -------------- Merna ------------------
    // -------------- End Merna ------------------
    // -------------- Roma ------------------
    // -------------- End roma ------------------
    // -------------- Motyim ------------------
    @Override
    public synchronized boolean isOnline() throws RemoteException {
        return true;
    }

    public synchronized void setAllUserOffline() {
        try {
            getConnection();
            query = "update UserTable set status='offline'";
            statement = connection.createStatement();
            statement.executeUpdate(query);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
    }

    @Override
    public synchronized String getGender(String username) {
        String gender = null;
        try {
            getConnection();
            query = "select * from UserTable where username='" + username + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                gender = resultSet.getString("gender");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return gender;
    }

    @Override
    public synchronized boolean updateUser(User user) throws RemoteException {
        try {
            getConnection();
            query = "update UserTable set email='" + user.getEmail() + "',fname='" + user.getFname() + "',lname='"
                    + user.getLname() + "',gender='" + user.getGender() + "',country='" + user.getCountry()
                    + "',image='"
                    + user.getImage() + "' where username='" + user.getUsername() + "'";
            statement = connection.createStatement();
            statement.executeUpdate(query);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeResources();
        }
    }

    @Override
    public synchronized User getUser(String userName) {
        try {
            getConnection();
            query = "select * from UserTable where username='" + userName + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                User user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFname(resultSet.getString("fname"));
                user.setLname(resultSet.getString("lname"));
                user.setPassword(resultSet.getString("password"));
                user.setGender(resultSet.getString("gender"));
                user.setCountry(resultSet.getString("country"));
                user.setStatus(resultSet.getString("status"));
                user.setImage(resultSet.getString("image"));
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return null;
    }

    // -------------- End Merna ------------------

    // -------------- Roma ------------------
    // -------------- End roma ------------------
    // -------------- Motyim ------------------
    // -------------- End motyim ------------------

    /**
     * Forces a refresh of the user data from the database
     * Used when client needs to ensure fresh data
     *
     * @param username the username to refresh
     * @return the refreshed User object
     * @throws RemoteException
     */
    @Override
    public User refreshUser(String username) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        try {
            System.out.println("Refreshing user data for: " + username);
            User user = getUser(username);
            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized void sendVoiceMessage(String sender, String receiver, VoiceMessage voiceMessage)
            throws RemoteException {
        System.out.println("ServerModel: Received voice message from " + sender + " to " + receiver);
        try {
            // Validate the message
            if (voiceMessage == null) {
                throw new RemoteException("Voice message cannot be null");
            }
            if (sender == null || sender.trim().isEmpty()) {
                throw new RemoteException("Sender cannot be null or empty");
            }
            if (receiver == null || receiver.trim().isEmpty()) {
                throw new RemoteException("Receiver cannot be null or empty");
            }
            if (voiceMessage.getAudioData() == null || voiceMessage.getAudioData().length == 0) {
                throw new RemoteException("Voice message audio data cannot be null or empty");
            }

            // Forward to controller
            controller.sendVoiceMessage(voiceMessage);
            System.out.println("ServerModel: Successfully forwarded voice message to controller");
        } catch (RemoteException e) {
            System.err.println("ServerModel: RemoteException while sending voice message: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("ServerModel: Unexpected error sending voice message: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Failed to process voice message: " + e.getMessage());
        }
    }
}
