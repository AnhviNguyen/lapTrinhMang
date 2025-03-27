/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utilitez.Checks;

/**
 * FXML Controller class
 *
 * @author Merna
 */
public class LaunchSceneController implements Initializable {

    @FXML
    private TextField txtFieldHostIP;
    @FXML
    private Button btnConnect;
    @FXML
    private Label status;

    ClientView clinetView;

    public LaunchSceneController() {
        clinetView = ClientView.getInstance();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (txtFieldHostIP == null) {
            txtFieldHostIP = new TextField();
        }
        // Set default IP address to localhost
        txtFieldHostIP.setText("localhost");

        // Try to connect automatically when application starts
        tryToConnect();
    }

    /**
     * Attempts to connect to the server using the IP in the text field
     */
    private void tryToConnect() {
        status.setText("Connecting to server...");

        String ip = txtFieldHostIP.getText().trim();

        // If text field is empty, default to localhost
        if (ip.isEmpty()) {
            ip = "localhost";
            txtFieldHostIP.setText(ip);
        }

        if (!ip.equals("localhost") && !Checks.checkIP(ip)) {
            status.setText("Invalid IP address. Please enter a valid IP address.");
            return;
        }

        String finalIp = ip;
        new Thread(() -> {
            try {
                boolean connected = clinetView.conncetToServer(finalIp);

                javafx.application.Platform.runLater(() -> {
                    if (connected) {
                        status.setText("Connected successfully!");
                        try {
                            Parent parent = FXMLLoader.load(getClass().getResource("LoginScene.fxml"));
                            Stage stage = clinetView.getMainStage();
                            Scene scene = new Scene(parent);
                            stage.setScene(scene);
                        } catch (IOException ex) {
                            Logger.getLogger(LaunchSceneController.class.getName()).log(Level.SEVERE, null, ex);
                            clinetView.showError("Navigation Error", "Error Loading Login Screen", ex.getMessage());
                        }
                    } else {
                        status.setText("Failed to connect. Check server or try another IP.");
                        btnConnect.setText("Retry Connection");
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    status.setText("Error: " + ex.getMessage());
                    btnConnect.setText("Retry Connection");
                });
            }
        }).start();
    }

    @FXML
    void btnConnectAction(ActionEvent event) {
        tryToConnect();
    }
}
