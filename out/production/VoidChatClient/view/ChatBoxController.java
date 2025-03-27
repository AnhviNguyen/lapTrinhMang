/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import model.ClientModelInt;
import model.Message;
import javafx.scene.shape.Circle;

/**
 * FXML Controller class
 *
 * @author Merna
 */
public class ChatBoxController implements Initializable {

    @FXML
    private VBox chatBox;
    @FXML
    private Label labelFriendName;
    @FXML
    private ImageView imgFriendStatus;
    @FXML
    private Label labelFriendStatus;
    @FXML
    private TextField txtFieldMsg;
    @FXML
    private Button btnSendAttach;
    @FXML
    private Image clips;
    @FXML
    private ListView<HBox> listviewChat;
    @FXML
    private Button saveBtn;
    @FXML
    private ToggleButton boldToggleBtn;

    @FXML
    private ToggleButton italicTogglebtn;

    @FXML
    private ToggleButton lineToggleBtn;

    @FXML
    private ComboBox<String> fontComboBox;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private ComboBox<String> fontSizeComboBox;

    @FXML
    private Button btnSendEmail;

    ClientView clientView;
    String receiver;
    Message message;

    ArrayList<Message> History = new ArrayList<>();

    Boolean recMsgFlag = true;
    Boolean sendMsgFlag = true;
    Boolean conFlag = false;

    private ChatBoxController() {
        clientView = ClientView.getInstance();
        clientView.chatBoxController = this;
    }

    // 3amlt deh
    public ChatBoxController(String receiver) {
        this();
        this.receiver = receiver;
    }

    public ChatBoxController(Message message) {
        this();
        this.message = message;

        if (!message.getTo().contains("##")) {
            receiver = message.getFrom();
            conFlag = true;
        }

    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        customizeEditorPane();
        if ((message != null && message.getTo().contains("##")) || (receiver != null && receiver.contains("##"))) {
            btnSendAttach.setDisable(true);
            saveBtn.setDisable(true);
            btnSendEmail.setDisable(true);
        }

        // Apply CSS styling
        File cssFile = new File("src/resouces/chatBoxStyle.css");
        if (cssFile.exists()) {
            listviewChat.getStylesheets().add("file:///" + cssFile.getAbsolutePath().replace("\\", "/"));
        } else {
            System.out.println("CSS file not found: " + cssFile.getAbsolutePath());
        }

        // Set cell factory for list view to make it transparent with proper spacing
        listviewChat.setCellFactory(lv -> new javafx.scene.control.ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
                setPadding(new Insets(4, 0, 4, 0));
                setStyle("-fx-background-color: transparent;");
            }
        });

        // Set proper width for list view
        listviewChat.setFixedCellSize(-1); // Variable height cells

        if (clientView.getHistory(receiver) != null) {
            loadHistory(clientView.getHistory(receiver));
        }

        // Style buttons
        btnSendAttach.getStyleClass().add("emoji-button");
        btnSendAttach.setTooltip(new Tooltip("Send Attachment"));

        saveBtn.getStyleClass().add("emoji-button");
        saveBtn.setTooltip(new Tooltip("Save Chat"));

        btnSendEmail.getStyleClass().add("emoji-button");
        btnSendEmail.setTooltip(new Tooltip("Send Email"));

        // Add padding to chat box container
        if (chatBox != null) {
            chatBox.setPadding(new Insets(10, 10, 10, 10));
            chatBox.getStyleClass().add("chat-box");
        }

        // Style the text field
        if (txtFieldMsg != null) {
            txtFieldMsg.getStyleClass().add("message-input");
            txtFieldMsg.setPromptText("Aa");
        }

        // Apply styles to format buttons
        boldToggleBtn.getStyleClass().add("emoji-button");
        italicTogglebtn.getStyleClass().add("emoji-button");
        lineToggleBtn.getStyleClass().add("emoji-button");
    }

    @FXML
    void saveBtnAction(ActionEvent event) {
        Platform.runLater(() -> {

            Stage st = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FileChooser fileChooser = new FileChooser();
            // Set extension filter
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("xml files (*.xml)", "*.xml"));
            // Show save file dialog
            File file = fileChooser.showSaveDialog(st);

            if (file != null) {
                ArrayList<Message> history = clientView.getHistory(receiver);
                clientView.saveXMLFile(file, history);
            }

        });
    }

    @FXML
    private void btnSendAttachAction(ActionEvent event) {

        Stage st = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();

        // Show save file dialog
        File file = fileChooser.showOpenDialog(st);

        // no file choosen
        if (file == null) {
            return;
        }

        // make connection with peer client
        ClientModelInt peer = clientView.getConnection(receiver);

        // peer user is offline
        if (peer == null) {
            clientView.showError("Offline", "Offline User", "User you try to connect is offline right now");
            return;
        }

        Thread tr = new Thread(() -> {
            try {
                FileInputStream in = null;

                // get path to save file on other user
                String path = peer.getSaveLocation(clientView.getUserInformation().getUsername(), file.getName());
                // other client refuse file transfare
                if (path == null) {

                    Platform.runLater(() -> {
                        clientView.showError("Refuse", "Request refused", "User not accept your file trans request");
                    });
                    return;
                }

                in = new FileInputStream(file);
                byte[] data = new byte[1024 * 1024];
                int dataLength = in.read(data);
                boolean append = false;
                while (dataLength > 0) {
                    peer.reciveFile(path, file.getName(), append, data, dataLength);
                    dataLength = in.read(data);
                    append = true;
                }

            } catch (RemoteException ex) {
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        tr.start();
    }

    private void sendMessageAction() {
        if (!txtFieldMsg.getText().trim().equals("")) {
            sendMsgFlag = true;

            // String color = "#" + Integer.toHexString(colorPicker.getValue().hashCode());
            String color = toRGBCode(colorPicker.getValue());
            String weight = (boldToggleBtn.isSelected()) ? "Bold" : "normal";
            String size = fontSizeComboBox.getSelectionModel().getSelectedItem();
            String style = (italicTogglebtn.isSelected()) ? "italic" : "normal";
            String font = fontComboBox.getSelectionModel().getSelectedItem();
            Boolean underline = lineToggleBtn.isSelected();

            Message msg = new Message();
            msg.setFontsSize(Integer.parseInt(size));
            msg.setFontColor(color);
            msg.setBody(txtFieldMsg.getText());
            msg.setFontWeight(weight);
            msg.setFontFamily(font);
            msg.setFrom(clientView.getUserInformation().getUsername());
            msg.setFontStyle(style);
            msg.setTo(receiver);
            msg.setDate(getXMLGregorianCalendarNow());
            msg.setUnderline(underline);

            clientView.sendMsg(msg);

            // Create outer container to align content to the right
            HBox container = new HBox();
            container.setMaxWidth(listviewChat.getWidth());
            container.setAlignment(Pos.CENTER_RIGHT);
            container.setPrefWidth(listviewChat.getWidth() - 20);

            // Create message bubble with proper margins
            VBox messageBox = new VBox();
            messageBox.setMaxWidth(300);
            messageBox.setPadding(new Insets(5, 5, 5, 5));
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setSpacing(4);

            // Add padding to prevent sticking to the edge
            HBox.setMargin(messageBox, new Insets(0, 10, 0, 80));

            // Message content
            Label sendLabel = new Label(txtFieldMsg.getText());
            sendLabel.setMaxWidth(300);
            sendLabel.setWrapText(true);
            sendLabel.getStyleClass().add("sender-message");
            sendLabel.setStyle("-fx-font-weight:" + weight
                    + ";-fx-font-size:" + size
                    + ";-fx-font-style:" + style
                    + ";-fx-font-family:\"" + font
                    + "\";-fx-underline:" + underline
                    + ";");

            // Create timestamp and status container (horizontal)
            HBox statusBox = new HBox();
            statusBox.setAlignment(Pos.CENTER_RIGHT);
            statusBox.setSpacing(5);

            // Add timestamp
            Text timestamp = new Text(getCurrentTime());
            timestamp.getStyleClass().add("message-timestamp");

            // Add read receipt indicator
            Text readStatus = new Text("Sent");
            readStatus.getStyleClass().add("message-timestamp");
            readStatus.setStyle("-fx-fill: #999999;");

            // Add components to status box
            statusBox.getChildren().addAll(readStatus, timestamp);

            // Add message components to container
            messageBox.getChildren().addAll(sendLabel, statusBox);
            container.getChildren().add(messageBox);

            // Add to listview
            listviewChat.getItems().add(container);
            listviewChat.scrollTo(container);
            txtFieldMsg.setText(null);
        }
    }

    public void reciveMsg(Message message) throws IOException {
        // Msg received in initialize don't send it again
        if (conFlag) {
            conFlag = false;
            return;
        }

        Boolean groupFlag = false;
        // hey there is new received msg, you will send the next msg with image
        recMsgFlag = true;
        if (message.getTo().contains("##")) {
            receiver = message.getTo();
            groupFlag = true;
        } else {
            receiver = message.getFrom();
        }

        if (message != null) {
            // Create outer container to align content to the left
            HBox container = new HBox();
            container.setMaxWidth(listviewChat.getWidth());
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPrefWidth(listviewChat.getWidth() - 20);

            // Create message bubble with proper margins
            VBox messageBox = new VBox();
            messageBox.setMaxWidth(300);
            messageBox.setPadding(new Insets(5, 5, 5, 5));
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setSpacing(4);

            // Add padding to prevent sticking to the edge
            HBox.setMargin(messageBox, new Insets(0, 80, 0, 10));

            // Add sender name if it's a group message
            if (groupFlag) {
                Text recName = new Text(message.getFrom());
                recName.setStyle("-fx-font: 10 arial; -fx-fill: #3578e5; -fx-font-weight: bold;");
                VBox.setMargin(recName, new Insets(0, 0, 3, 5));
                messageBox.getChildren().add(recName);
            }

            // Create message label
            Label recLabel = new Label(message.getBody());
            recLabel.setMaxWidth(300);
            recLabel.setWrapText(true);
            recLabel.getStyleClass().add("receiver-message");
            recLabel.setStyle("-fx-font-weight:" + message.getFontWeight()
                    + ";-fx-font-size:" + message.getFontsSize()
                    + ";-fx-font-style:" + message.getFontStyle()
                    + ";-fx-font-family:\"" + message.getFontFamily()
                    + "\";-fx-underline:" + message.getUnderline()
                    + ";-fx-text-fill:" + message.getFontColor()
                    + ";");

            // Add timestamp
            String timestamp = formatTimestampFromMessage(message);
            Text timeText = new Text(timestamp);
            timeText.getStyleClass().add("message-timestamp");

            // Add message components
            messageBox.getChildren().addAll(recLabel, timeText);
            container.getChildren().add(messageBox);

            listviewChat.getItems().add(container);
            listviewChat.scrollTo(container);
        }
    }

    // handle Enter pressed action on txtFieldMessage and call the sendMessageAction
    // ..
    @FXML
    private void txtFieldOnKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            sendMessageAction();
        }
    }

    @FXML
    void btnSendEmailAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SendEmailScene.fxml"));
            String to = clientView.getUser(receiver).getEmail();
            loader.setController(new SendEmailSceneController(clientView.getUserInformation().getEmail(), to));
            Parent parent = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Send Email");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * customize Editor pane with styles (bold,italic,font,size ..)
     */
    void customizeEditorPane() {
        ObservableList<String> limitedFonts = FXCollections.observableArrayList("Arial", "Times", "Courier New",
                "Comic Sans MS");
        fontComboBox.setItems(limitedFonts);
        fontComboBox.getSelectionModel().selectFirst();

        ObservableList<String> fontSizes = FXCollections.observableArrayList("8", "10", "12", "14", "18", "24");
        fontSizeComboBox.setItems(fontSizes);
        fontSizeComboBox.getSelectionModel().select(2);

        colorPicker.setValue(Color.BLACK);
    }

    public XMLGregorianCalendar getXMLGregorianCalendarNow() {
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            return now;
        } catch (DatatypeConfigurationException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void loadHistory(ArrayList<Message> messages) {
        for (Message message : messages) {
            try {
                // Create outer container
                HBox container = new HBox();
                container.setMaxWidth(listviewChat.getWidth());
                container.setPrefWidth(listviewChat.getWidth() - 20);

                // Create message bubble with proper margins
                VBox messageBox = new VBox();
                messageBox.setMaxWidth(300);
                messageBox.setPadding(new Insets(5, 5, 5, 5));
                messageBox.setSpacing(4);

                // Create message label
                Label messageLabel = new Label(message.getBody());
                messageLabel.setMaxWidth(300);
                messageLabel.setWrapText(true);

                // Format timestamp
                String timeStr = formatTimestampFromMessage(message);
                Text timestamp = new Text(timeStr);
                timestamp.getStyleClass().add("message-timestamp");

                // Check if this message is from the current user
                if (message.getFrom().equals(clientView.getUserInformation().getUsername())) {
                    // Sent message - display on right
                    container.setAlignment(Pos.CENTER_RIGHT);
                    messageBox.setAlignment(Pos.CENTER_RIGHT);

                    // Add margin to prevent sticking to edge
                    HBox.setMargin(messageBox, new Insets(0, 10, 0, 80));

                    // Style for sent messages
                    messageLabel.getStyleClass().add("sender-message");
                    messageLabel.setStyle("-fx-font-weight:" + message.getFontWeight()
                            + ";-fx-font-size:" + message.getFontsSize()
                            + ";-fx-font-style:" + message.getFontStyle()
                            + ";-fx-font-family:\"" + message.getFontFamily()
                            + "\";-fx-underline:" + message.getUnderline()
                            + ";");

                    // Right-align timestamp
                    timestamp.setStyle("-fx-text-alignment: right;");

                } else {
                    // Received message - display on left
                    container.setAlignment(Pos.CENTER_LEFT);
                    messageBox.setAlignment(Pos.CENTER_LEFT);

                    // Add margin to prevent sticking to edge
                    HBox.setMargin(messageBox, new Insets(0, 80, 0, 10));

                    // Add sender name if group message
                    if (message.getTo().contains("##")) {
                        Text senderName = new Text(message.getFrom());
                        senderName.setStyle("-fx-font: 10 arial; -fx-fill: #3578e5; -fx-font-weight: bold;");
                        VBox.setMargin(senderName, new Insets(0, 0, 3, 5));
                        messageBox.getChildren().add(senderName);
                    }

                    // Style for received messages
                    messageLabel.getStyleClass().add("receiver-message");
                    messageLabel.setStyle("-fx-font-weight:" + message.getFontWeight()
                            + ";-fx-font-size:" + message.getFontsSize()
                            + ";-fx-font-style:" + message.getFontStyle()
                            + ";-fx-font-family:\"" + message.getFontFamily()
                            + "\";-fx-underline:" + message.getUnderline()
                            + ";-fx-text-fill:" + message.getFontColor()
                            + ";");
                }

                // Add message content and timestamp to the message box
                messageBox.getChildren().addAll(messageLabel, timestamp);
                container.getChildren().add(messageBox);

                // Add to listview
                listviewChat.getItems().add(container);

            } catch (Exception ex) {
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Scroll to the last message
        if (!messages.isEmpty()) {
            listviewChat.scrollTo(listviewChat.getItems().size() - 1);
        }
    }

    public void sendMail() {
        // TODO : edit this one
        clientView.sendMail(receiver, receiver);
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Add reference to ChatSceneController
    private ChatSceneController chatSceneController;

    /**
     * Sets the ChatSceneController reference
     * 
     * @param chatSceneController The ChatSceneController instance
     */
    public void setChatSceneController(ChatSceneController chatSceneController) {
        this.chatSceneController = chatSceneController;
    }

    /**
     * Initialize the chat box with the friend's name
     * 
     * @param friendName The friend's username
     */
    public void init(String friendName) {
        this.receiver = friendName;

        // Set friend's name in the label
        if (labelFriendName != null) {
            labelFriendName.setText(friendName);
            labelFriendName.getStyleClass().add("friend-name");
        }

        // Load friend's status if available
        if (clientView != null && labelFriendStatus != null) {
            String status = clientView.getStatus(friendName);
            labelFriendStatus.getStyleClass().add("friend-status");

            if (status != null) {
                // Create a Circle status indicator
                Circle statusIndicator = new Circle(5);
                statusIndicator.setTranslateY(0);

                switch (status) {
                    case "online":
                        labelFriendStatus.setText("Active Now");
                        statusIndicator.getStyleClass().add("status-indicator-online");
                        break;
                    case "busy":
                        labelFriendStatus.setText("Busy");
                        statusIndicator.getStyleClass().add("status-indicator-busy");
                        break;
                    case "offline":
                    default:
                        labelFriendStatus.setText("Offline");
                        statusIndicator.getStyleClass().add("status-indicator-offline");
                        break;
                }

                // Create a horizontal box to hold the indicator and text
                HBox statusBox = new HBox(5); // 5px spacing
                statusBox.setAlignment(Pos.CENTER_LEFT);

                // Add circle and existing label to the box
                statusBox.getChildren().addAll(statusIndicator, new Text(labelFriendStatus.getText()));

                // Clear the existing label and set the content
                labelFriendStatus.setGraphic(statusBox);
                labelFriendStatus.setText("");
            }
        }

        // Set appropriate image for friend status
        if (imgFriendStatus != null && clientView != null) {
            try {
                // Set user image based on gender
                String gender = clientView.getGender(friendName);
                Image userImage;

                if (gender != null && gender.equals("Female")) {
                    userImage = new Image(getClass().getResource("/resouces/female_32.png").openStream());
                } else {
                    userImage = new Image(getClass().getResource("/resouces/user_32.png").openStream());
                }

                imgFriendStatus.setImage(userImage);

                // Make profile image circular
                Circle clip = new Circle(16, 16, 16);
                imgFriendStatus.setClip(clip);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Load chat history if available and listview is empty
        if (clientView != null && listviewChat != null && listviewChat.getItems().isEmpty() &&
                clientView.getHistory(friendName) != null) {
            loadHistory(clientView.getHistory(friendName));
        }
    }

    /**
     * Get current time as a formatted string for timestamps
     * 
     * @return Formatted time string (e.g., "10:30 AM")
     */
    private String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
        return now.format(formatter);
    }

    /**
     * Format timestamp from a message
     * 
     * @param message The message containing timestamp
     * @return Formatted time string
     */
    private String formatTimestampFromMessage(Message message) {
        if (message.getDate() != null) {
            try {
                GregorianCalendar cal = message.getDate().toGregorianCalendar();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
                java.time.LocalTime time = java.time.LocalTime.of(
                        cal.get(java.util.Calendar.HOUR_OF_DAY),
                        cal.get(java.util.Calendar.MINUTE));
                return time.format(formatter);
            } catch (Exception e) {
                return getCurrentTime();
            }
        }
        return getCurrentTime();
    }
}
