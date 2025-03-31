package model;

import controller.ServerController;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerPrivateModel {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String query;
    private String property = System.getProperty("user.dir");
    private ServerController controller;

    public ServerPrivateModel(ServerController controller) throws RemoteException {
        this.controller = controller;
    }

    /**
     * connect to database
     */
    private void getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + property + "/voidChat", "", "");
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * close connection to database
     */
    private void closeResources() {
        try {
            // resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public User getUserInfo(String username) {
        User user = null;
        try {
            getConnection();
            query = "select * from UserTable where username='" + username + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                user = new User();
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFname(resultSet.getString("fname"));
                user.setLname(resultSet.getString("lname"));
                user.setPassword(resultSet.getString("password"));
                user.setGender(resultSet.getString("gender"));
                user.setCountry(resultSet.getString("country"));
                user.setStatus(resultSet.getString("status"));
                user.setImage(resultSet.getString("image"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        closeResources();
        return user;
    }
}
