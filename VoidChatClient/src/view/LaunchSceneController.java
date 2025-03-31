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
        btnConnect.setDisable(true);
        btnConnect.setText("Connecting...");

        new Thread(() -> {
            try {
                // Set a timeout for connection attempts
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newSingleThreadExecutor();
                java.util.concurrent.Future<Boolean> future = executor
                        .submit(() -> clinetView.conncetToServer(finalIp));

                boolean connected = false;
                try {
                    // Wait for connection with timeout (8 seconds)
                    connected = future.get(8, java.util.concurrent.TimeUnit.SECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    future.cancel(true);
                    throw new Exception("Connection timed out. Server may be down or unreachable.");
                } catch (java.util.concurrent.ExecutionException e) {
                    throw e.getCause() instanceof Exception ? (Exception) e.getCause()
                            : new Exception("Connection error: " + e.getMessage());
                } finally {
                    executor.shutdownNow();
                }

                final boolean connectionResult = connected;
                javafx.application.Platform.runLater(() -> {
                    btnConnect.setDisable(false);

                    if (connectionResult) {
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
                        showConnectionHelp();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                final String errorMsg = ex.getMessage();
                javafx.application.Platform.runLater(() -> {
                    btnConnect.setDisable(false);
                    status.setText("Error: " + errorMsg);
                    btnConnect.setText("Retry Connection");
                    showConnectionHelp();
                });
            }
        }).start();
    }

    /**
     * Shows connection troubleshooting tips
     */
    private void showConnectionHelp() {
        String troubleshootingTips = "Connection Troubleshooting Tips:\n" +
                "1. Check if the server is running\n" +
                "2. Ensure you're connected to the same network as the server\n" +
                "3. Verify firewall settings aren't blocking the connection\n" +
                "4. Try using 'localhost' if running server locally\n" +
                "5. Check that port 1050 is open on the server";

        clinetView.showError("Connection Failed", "Unable to connect to server", troubleshootingTips);
    }

    @FXML
    void btnConnectAction(ActionEvent event) {
        tryToConnect();
    }
}
