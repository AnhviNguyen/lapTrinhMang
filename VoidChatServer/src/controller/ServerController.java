package controller;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import model.ClientModelInt;
import model.MailModel;
import model.Message;
import model.ServerModel;
import model.ServerPrivateModel;
import model.User;
import model.UserFx;
import model.VoiceMessage;
import utilitez.Notification;
import utilitez.Pair;
import view.ServerView;

public class ServerController implements ServerControllerInt {

    private HashMap<String, ClientModelInt> onlineUsers = new HashMap<>();
    private HashMap<String, ArrayList<String>> groups = new HashMap<>();

    private ServerModel model;
    private ServerView view;

    private Registry reg;

    private ServerPrivateModel privateModel;

    private byte[] sponserImage;
    private String serverNotifaction;

    private Thread checkOnline;

    public ServerController(ServerView view) {
        try {

            // conncet to view
            this.view = view;
            // connect to model
            model = new ServerModel(this);

            // connect to private model
            privateModel = new ServerPrivateModel(this);

            // upload to registry
            reg = LocateRegistry.createRegistry(1050);

            serverNotifaction = "Void Chat Team Yor7b bekom :) ";

        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void startServer() {
        try {
            reg.rebind("voidChatServer", model);

            // method to check online users
            checkOnline = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        checkOnlines();
                        // Add a sleep between checks to prevent excessive CPU usage
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // Thread was interrupted during sleep, exit the loop
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Server check thread terminated due to error: " + e.getMessage());
                }
            });
            checkOnline.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM exit
            checkOnline.start();

            // set all users offline
            model.setAllUserOffline();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            System.err.println("Failed to start server: " + ex.getMessage());
        }
    }

    @Override
    public void stopServer() {
        try {
            reg.unbind("voidChatServer");
            // Thay vì checkOnline.stop(), sử dụng interrupt() cho Thread
            if (checkOnline != null) {
                checkOnline.interrupt(); // Ngắt thread một cách an toàn
                checkOnline = null; // Đặt lại tham chiếu nếu cần
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Application.launch(ServerView.class, args);
    }

    @Override
    public void notify(String reciver, String message, int type) {

        if (onlineUsers.containsKey(reciver)) {

            ClientModelInt clientObject = onlineUsers.get(reciver);
            try {
                clientObject.notify(message, type);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void recieveMsg(Message message) {

        String reciever = message.getTo();

        if ((reciever.contains("##"))) {
            if (groups.containsKey(reciever)) {
                ArrayList<String> chatMembers = groups.get(reciever);

                for (int i = 0; i < chatMembers.size(); i++) {

                    if (!chatMembers.get(i).equals(message.getFrom())) {
                        if (onlineUsers.containsKey(chatMembers.get(i))) {

                            try {

                                ClientModelInt clientObject = onlineUsers.get(chatMembers.get(i));

                                clientObject.reciveMsg(message);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else if (onlineUsers.containsKey(reciever)) {
            ClientModelInt clientObject = onlineUsers.get(reciever);
            try {

                clientObject.reciveMsg(message);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean register(String username, ClientModelInt obj) {
        if (onlineUsers.containsKey(username)) {
            return false;
        }
        onlineUsers.put(username, obj);
        sendServerNotifcation(obj); // update message and sponcer in recently login user

        return true;
    }

    @Override
    public User getUserInfo(String username) {
        return privateModel.getUserInfo(username);
    }

    @Override
    public void sendAnnouncement(String message) {

        // save on local
        serverNotifaction = message;

        Set<String> onlineSet = onlineUsers.keySet();
        onlineSet.forEach((user) -> {
            try {
                onlineUsers.get(user).receiveAnnouncement(message);
            } catch (RemoteException ex) {
                Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    @Override
    public void unregister(String username) {

        onlineUsers.remove(username);

    }

    @Override
    public ClientModelInt getConnection(String Client) {

        if (onlineUsers.containsKey(Client)) {
            return onlineUsers.get(Client);
        }

        return null;
    }

    @Override
    public void createGroup(String groupName, ArrayList<String> groupMembers) {
        // Create group in database
        model.createGroup(groupName, groupMembers);

        // Notify all members about the new group
        for (String member : groupMembers) {
            ClientModelInt connection = onlineUsers.get(member);
            if (connection != null) {
                try {
                    // Send group creation notification
                    connection.notify("GROUP_ADDED:" + groupName, Notification.GENERAL);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendSponser(byte[] data, int dataLength) {
        sponserImage = new byte[dataLength];

        // copy bytes in local array
        for (int i = 0; i < dataLength; i++) {
            sponserImage[i] = data[i];
        }

        // get all online users
        Set<String> onlineSet = onlineUsers.keySet();
        onlineSet.forEach((user) -> {
            try {
                onlineUsers.get(user).reciveSponser(sponserImage, dataLength);
            } catch (RemoteException ex) {
                Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void sendServerNotifcation(ClientModelInt obj) {
        Thread tr = new Thread() {
            @Override
            public void run() {

                try {
                    Thread.sleep(5000); // wait untile chatbox load

                    obj.receiveAnnouncement(serverNotifaction);

                    if (sponserImage != null) {
                        obj.reciveSponser(sponserImage, sponserImage.length);
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };

        tr.start();

    }

    @Override
    public boolean sendMail(String to, String subject, String emailBody) {
        return new MailModel(to, subject, emailBody).sendMail();
    }

    /**
     * send welcome mail for first sign up user
     *
     * @param mail
     * @param username
     * @param password
     */
    public void sendWelcomeMail(String mail, String username, String password) {
        Thread tr = new Thread(() -> {
            String message = "<h1>Welcome to Void Chat</h1> <br/> your username: "
                    + username + "<br/>your password : " + password
                    + "<br/>we are waiting for you .. just login and have fun ;)";
            sendMail(mail, "Welcome To Void Chat", message);
        });
        tr.start();
    }

    public ArrayList<User> getAllUsers() {
        if (model.getAllUsers() != null) {
            return model.getAllUsers();
        }
        return null;
    }

    @Override
    public void updateUser(User user) throws RemoteException {
        try {
            model.updateUser(user);
            // Notify all clients about the avatar update
            notifyAvatarUpdate(user.getUsername());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void GenerateUserFX(UserFx user) {
        view.GenerateUserFX(user);

    }

    // -------------- Merna ------------------
    public ArrayList<Integer> getStatistics() {
        try {
            if (model != null) {
                return model.getStatistics();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return empty list if model is null or exception occurs
        return new ArrayList<>();
    }

    public ArrayList<Pair> getCountries() {
        try {
            if (model != null) {
                return model.getCountries();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return empty list if model is null or exception occurs
        return new ArrayList<>();
    }

    public ArrayList<Pair> getGender() {
        try {
            if (model != null) {
                return model.getGender();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return empty list if model is null or exception occurs
        return new ArrayList<>();
    }
    // -------------- End Merna ------------------

    // -------------- Roma ------------------
    // -------------- End roma ------------------
    // -------------- Motyim ------------------

    // method to check online users and remove not active user
    private void checkOnlines() {
        try {
            Thread.sleep(5000);

            // Create a copy of keys to avoid ConcurrentModificationException
            ArrayList<String> usersToCheck = new ArrayList<>(onlineUsers.keySet());

            for (String user : usersToCheck) {
                try {
                    // Add a timeout to the check to prevent connection hanging
                    ClientModelInt client = onlineUsers.get(user);
                    if (client == null)
                        continue;

                    // Set a timeout for the connection check
                    boolean isOnline = false;
                    try {
                        isOnline = client.isOnline();
                    } catch (RemoteException ex) {
                        System.out.println("User " + user + " disconnected: " + ex.getMessage());
                        // handle set user offline
                        try {
                            model.changeStatus(user, "offline");
                            // remove user from hashmap safely
                            onlineUsers.remove(user);
                        } catch (RemoteException ex1) {
                            Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    } catch (Exception ex) {
                        System.out.println("Error checking user " + user + ": " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
            // Thread was interrupted, exit the loop
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // -------------- End motyim ------------------

    @Override
    public void loadErrorServer() {
        for (String key : onlineUsers.keySet()) {
            ClientModelInt clientObject = onlineUsers.get(key);
            try {
                clientObject.loadErrorServer();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Notify all clients that a user has updated their avatar
     * 
     * @param username The username of the user who updated their avatar
     */
    @Override
    public void notifyAvatarUpdate(String username) {
        // Log the action
        System.out.println("Broadcasting avatar update for user: " + username);

        try {
            // Create a list to store any failed notifications
            ArrayList<String> failedClients = new ArrayList<>();

            // Notify all online users about the avatar update
            for (String onlineUser : onlineUsers.keySet()) {
                try {
                    ClientModelInt clientObject = onlineUsers.get(onlineUser);
                    if (clientObject != null) {
                        System.out.println("Notifying client: " + onlineUser + " about avatar update for: " + username);
                        clientObject.receiveAvatarUpdate(username);
                    }
                } catch (RemoteException ex) {
                    System.out.println(
                            "Failed to notify client " + onlineUser + " about avatar update: " + ex.getMessage());
                    failedClients.add(onlineUser);
                }
            }

            // Remove any failed clients from the online users list
            for (String failedClient : failedClients) {
                System.out.println("Removing failed client from online users: " + failedClient);
                onlineUsers.remove(failedClient);
            }

            System.out.println("Completed avatar update broadcast for user: " + username);
        } catch (Exception ex) {
            System.out.println("Error broadcasting avatar update: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void sendVoiceMessage(VoiceMessage voiceMessage) throws RemoteException {
        String receiver = voiceMessage.getTo();

        // Handle group voice messages
        if (receiver.contains("##")) {
            if (groups.containsKey(receiver)) {
                ArrayList<String> chatMembers = groups.get(receiver);

                for (String member : chatMembers) {
                    if (!member.equals(voiceMessage.getFrom())) {
                        if (onlineUsers.containsKey(member)) {
                            try {
                                ClientModelInt clientObject = onlineUsers.get(member);
                                clientObject.receiveVoiceMessage(voiceMessage);
                            } catch (RemoteException ex) {
                                Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE,
                                        "Failed to send voice message to " + member, ex);
                                // Remove failed client
                                onlineUsers.remove(member);
                            }
                        }
                    }
                }
            }
        }
        // Handle direct voice messages
        else if (onlineUsers.containsKey(receiver)) {
            try {
                ClientModelInt clientObject = onlineUsers.get(receiver);
                clientObject.receiveVoiceMessage(voiceMessage);
            } catch (RemoteException ex) {
                Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE,
                        "Failed to send voice message to " + receiver, ex);
                // Remove failed client
                onlineUsers.remove(receiver);
            }
        }
    }

    public HashMap<String, ClientModelInt> getOnlineUsers() {
        return onlineUsers;
    }

}
