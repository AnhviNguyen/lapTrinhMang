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
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import model.AudioRecorder;
import model.ClientModelInt;
import model.Message;
import javafx.scene.shape.Circle;
import model.User;
import javafx.scene.text.Font;
import model.VoiceMessage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.ListCell;

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
    private TextArea txtFieldMsg;
    @FXML
    private Button btnSendAttach;
    @FXML
    private ImageView clips;
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

    @FXML
    private Button btnEmoji;

    @FXML
    private Button btnVoiceRecord;

    // Voice recording related fields
    private AudioRecorder audioRecorder;
    private boolean isRecording = false;
    private String recordingTempPath;
    private Timeline recordingTimer;
    private int recordingDuration = 0;
    private Label recordingTimeLabel;
    private HBox recordingIndicator;

    ClientView clientView;
    String receiver;
    Message message;

    ArrayList<Message> History = new ArrayList<>();

    Boolean recMsgFlag = true;
    Boolean sendMsgFlag = true;
    Boolean conFlag = false;

    /**
     * Default constructor used by FXML loader
     */
    public ChatBoxController() {
        // Initialize only if not already initialized
        if (clientView == null) {
            clientView = ClientView.getInstance();
            clientView.chatBoxController = this;
        }
    }

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
        // Initialize friend info
        User friend = clientView.getUser(receiver);
        if (friend != null) {
            labelFriendName.setText(friend.getFname() + " " + friend.getLname());
            labelFriendStatus.setText(friend.getStatus());

            try {
                // Set friend's avatar with forced reload - safely
                Image image;
                if (friend.getImage() != null && !friend.getImage().trim().isEmpty()) {
                    image = loadImageWithCacheBusting(friend.getImage());
                } else {
                    // Set default avatar based on gender
                    String defaultImage = friend.getGender() != null && friend.getGender().equalsIgnoreCase("Female")
                            ? "/resouces/female.png"
                            : "/resouces/male.png";
                    image = loadImageWithCacheBusting(defaultImage);
                }

                // Set image and make it circular
                imgFriendStatus.setImage(image);
                imgFriendStatus.setFitWidth(32);
                imgFriendStatus.setFitHeight(32);
                imgFriendStatus.setPreserveRatio(true);
                makeCircularImage(imgFriendStatus, 16);
            } catch (Exception ex) {
                System.out.println("Error loading friend avatar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Configure TextArea properties
        txtFieldMsg.setWrapText(true);
        txtFieldMsg.setPrefRowCount(2);
        txtFieldMsg.setMaxHeight(40);
        txtFieldMsg.setPrefHeight(40);

        // Initialize formatting controls - must be called before
        // setupFormattingControls
        customizeEditorPane();

        // Style the font and font size combo boxes to make them more visible
        fontComboBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3; -fx-background-radius: 3;");
        fontSizeComboBox.setStyle(
                "-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3; -fx-background-radius: 3;");

        // Make the dropdowns wider for better visibility
        fontComboBox.setPrefWidth(110);
        fontSizeComboBox.setPrefWidth(60);

        // Add event handling
        setupFormattingControls();

        // Initialize voice recording components
        audioRecorder = new model.AudioRecorder();
        createRecordingIndicator();
    }

    @FXML
    void saveBtnAction(ActionEvent event) {
        Platform.runLater(() -> {
            try {
                // Kiểm tra xem có tin nhắn để lưu không
                ArrayList<Message> history = clientView.getHistory(receiver);
                if (history == null || history.isEmpty()) {
                    clientView.showError("Empty Chat", "No Messages", "There are no messages to save in this chat.");
                    return;
                }

                // Tạo dialog chọn file
                Stage st = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FileChooser fileChooser = new FileChooser();

                // Đặt tiêu đề cho dialog
                fileChooser.setTitle("Save Chat History");

                // Tạo tên file mặc định từ tên người dùng và thời gian hiện tại
                String defaultFileName = "Chat_" + clientView.getUserInformation().getUsername() +
                        "_" + receiver + "_" +
                        java.time.LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                fileChooser.setInitialFileName(defaultFileName);

                // Thiết lập bộ lọc file
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("XML Files (*.xml)", "*.xml"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

                // Hiển thị dialog lưu file
                File file = fileChooser.showSaveDialog(st);

                if (file != null) {
                    // Hiển thị thông báo đang lưu
                    System.out.println("Saving chat history to " + file.getAbsolutePath());

                    try {
                        // Thực hiện lưu file
                        clientView.saveXMLFile(file, history);

                        // Hiển thị thông báo lưu thành công
                        clientView.showSuccess("Success", "Chat History Saved",
                                "Chat history has been saved to:\n" + file.getAbsolutePath());
                    } catch (Exception e) {
                        // Hiển thị thông báo lỗi
                        clientView.showError("Error", "Save Failed",
                                "Could not save chat history: " + e.getMessage());
                        System.out.println("Error saving chat history: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                // Xử lý lỗi
                clientView.showError("Error", "Save Error",
                        "An error occurred while saving: " + ex.getMessage());
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
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
            clientView.showError("Offline", "Request refused", "User not accept your file trans request");
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

    @FXML
    private void sendMessageAction(ActionEvent event) {
        String text = txtFieldMsg.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        try {
            // Get all formatting attributes with null checks
            String fontFamily = fontComboBox.getValue();
            if (fontFamily == null) {
                fontFamily = "Arial";
            }

            String fontSize = fontSizeComboBox.getValue();
            if (fontSize == null) {
                fontSize = "12";
            }

            Boolean isBold = boldToggleBtn.isSelected();
            Boolean isItalic = italicTogglebtn.isSelected();
            Boolean isUnderlined = lineToggleBtn.isSelected();

            // Get color as hex with null check
            Color color = colorPicker.getValue();
            if (color == null) {
                color = Color.BLACK;
            }

            String colorHex = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            // Create message with formatting
            Message message = new Message();
            message.setFrom(clientView.getUserInformation().getUsername());
            message.setTo(receiver);
            message.setDate(getXMLGregorianCalendarNow());
            message.setBody(text);
            message.setFontFamily(fontFamily);

            // Safe parsing of font size
            int fontSizeInt = 12; // Default value
            try {
                fontSizeInt = Integer.parseInt(fontSize);
            } catch (NumberFormatException e) {
                System.out.println("Invalid font size format, using default: " + e.getMessage());
            }
            message.setFontsSize(fontSizeInt);

            message.setFontWeight(isBold ? "bold" : "normal");
            message.setFontStyle(isItalic ? "italic" : "normal");
            message.setFontColor(colorHex);
            message.setUnderline(isUnderlined);

            // Log formatting details for debugging
            System.out.println("Message formatting: " +
                    "font-family=" + fontFamily +
                    ", font-size=" + fontSizeInt +
                    ", font-weight=" + (isBold ? "bold" : "normal") +
                    ", font-style=" + (isItalic ? "italic" : "normal") +
                    ", color=" + colorHex +
                    ", underline=" + isUnderlined);

            try {
                // Send message using the client view
                clientView.sendMsg(message);

                // Display the message in the chat area
                File f = new File("client-send.css");
                sendMsgFlag = true;
                recMsgFlag = false;
                appendToHistoryBox(message, f.getAbsolutePath(), sendMsgFlag);

                // Clear the input field
                txtFieldMsg.clear();

                // Update the text area with current formatting
                updateTextAreaPreview();
            } catch (Exception ex) {
                System.out.println("Error sending message: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.out.println("Error creating message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Displays a sent message in the chat UI with proper spacing and avatar
     */
    private void displaySentMessage(Message message) {
        try {
            // Create outer container
            HBox container = new HBox(10);
            container.setMaxWidth(listviewChat.getWidth());
            container.setAlignment(Pos.CENTER_RIGHT);
            container.setPrefWidth(listviewChat.getWidth() - 20);
            // IMPORTANT: Use consistent 75px padding on left for space-between effect
            container.setPadding(new Insets(5, 5, 5, 75));

            // Store sender info in userData for future reference
            container.setUserData(message.getFrom());

            // Create message bubble
            VBox messageBox = new VBox(4);
            // IMPORTANT: Set consistent max width for messages
            messageBox.setMaxWidth(250);
            messageBox.setPadding(new Insets(8, 12, 8, 12));
            messageBox.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 15;");

            // Create message label
            Label messageLabel = new Label(message.getBody());
            messageLabel.setMaxWidth(250);
            messageLabel.setWrapText(true);

            // Apply text styling using helper method
            applyMessageFormatting(messageLabel, message, true);

            // Create timestamp
            String timeStr = formatTimestampFromMessage(message);
            Text timestamp = new Text(timeStr);
            timestamp.setFill(Color.WHITE);
            timestamp.setOpacity(0.8);
            timestamp.setStyle("-fx-font-size: 10px;");

            // Add components to boxes
            messageBox.getChildren().addAll(messageLabel, timestamp);

            // Create avatar
            ImageView avatar = new ImageView();
            avatar.setFitWidth(32);
            avatar.setFitHeight(32);
            // Disable caching to ensure fresh images
            avatar.setCache(false);

            // Get user avatar safely
            User currentUser = clientView.getUserInformation();
            if (currentUser != null) {
                String imagePath = null;
                if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                    imagePath = currentUser.getImage();
                    // Add cache busting parameter for URLs
                    if (imagePath.startsWith("http")) {
                        String separator = imagePath.contains("?") ? "&" : "?";
                        imagePath += separator + "t=" + System.currentTimeMillis();
                    }
                } else {
                    imagePath = currentUser.getGender() != null &&
                            currentUser.getGender().equalsIgnoreCase("Female") ? "/resouces/female.png"
                                    : "/resouces/male.png";
                }
                avatar.setImage(loadImageWithCacheBusting(imagePath));
            } else {
                avatar.setImage(loadImageWithCacheBusting("/resouces/male.png"));
            }

            makeCircularImage(avatar, 16);

            // Add avatar and message box to container
            container.getChildren().addAll(messageBox, avatar);

            // Add container to chat list
            listviewChat.getItems().add(container);
            listviewChat.scrollTo(container);

            System.out.println("Displayed sent message with consistent space-between layout");
        } catch (Exception e) {
            System.out.println("Error displaying sent message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Receives and displays a message in the chat UI
     */
    public void reciveMsg(Message message) throws IOException {
        try {
            // Skip if message was received during initialization
            if (conFlag) {
                conFlag = false;
                return;
            }

            // Set receiver based on message type
            boolean groupFlag = false;
            recMsgFlag = true;
            if (message.getTo().contains("##")) {
                receiver = message.getTo();
                groupFlag = true;
            } else {
                receiver = message.getFrom();
            }

            // Skip our own messages since we already showed them
            if (message.getFrom().equals(clientView.getUserInformation().getUsername()) &&
                    !message.getTo().contains("##")) {
                return;
            }

            // Create container with proper spacing for space-between layout
            HBox container = new HBox(10);
            container.setMaxWidth(listviewChat.getWidth());
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPrefWidth(listviewChat.getWidth() - 20);
            // IMPORTANT: Use consistent 75px padding on right for space-between effect
            container.setPadding(new Insets(5, 75, 5, 5));

            // Store sender info in userData for future reference
            container.setUserData(message.getFrom());

            // Create avatar image
            ImageView avatar = new ImageView();
            avatar.setFitWidth(32);
            avatar.setFitHeight(32);
            // Disable caching to ensure fresh images
            avatar.setCache(false);

            // Get sender avatar
            User sender = clientView.getUser(message.getFrom());
            if (sender != null) {
                String imagePath = null;
                if (sender.getImage() != null && !sender.getImage().isEmpty()) {
                    imagePath = sender.getImage();
                    // Add cache busting for URLs
                    if (imagePath.startsWith("http")) {
                        String separator = imagePath.contains("?") ? "&" : "?";
                        imagePath += separator + "t=" + System.currentTimeMillis();
                    }
                } else {
                    imagePath = sender.getGender() != null &&
                            sender.getGender().equalsIgnoreCase("Female") ? "/resouces/female.png"
                                    : "/resouces/male.png";
                }
                avatar.setImage(loadImageWithCacheBusting(imagePath));
            } else {
                avatar.setImage(loadImageWithCacheBusting("/resouces/male.png"));
            }

            makeCircularImage(avatar, 16);

            // Create message bubble
            VBox messageBox = new VBox(4);
            // IMPORTANT: Set consistent max width for messages (250px)
            messageBox.setMaxWidth(250);
            messageBox.setPadding(new Insets(8, 12, 8, 12));
            messageBox.setStyle("-fx-background-color: #e4e6eb; -fx-background-radius: 15;");

            // Add sender name for group messages
            if (groupFlag) {
                Text senderName = new Text(message.getFrom());
                senderName.setStyle("-fx-font-size: 11px; -fx-fill: #3578e5; -fx-font-weight: bold;");
                VBox.setMargin(senderName, new Insets(0, 0, 4, 0));
                messageBox.getChildren().add(senderName);
            }

            // Create message text
            Label messageLabel = new Label(message.getBody());
            messageLabel.setMaxWidth(250);
            messageLabel.setWrapText(true);

            // Apply text styling using helper method
            applyMessageFormatting(messageLabel, message, false);

            // Create timestamp
            String timeStr = formatTimestampFromMessage(message);
            Text timestamp = new Text(timeStr);
            timestamp.setFill(Color.GRAY);
            timestamp.setOpacity(0.8);
            timestamp.setStyle("-fx-font-size: 10px;");

            // Add components to boxes
            messageBox.getChildren().addAll(messageLabel, timestamp);
            container.getChildren().addAll(avatar, messageBox);

            // Add to chat list
            listviewChat.getItems().add(container);
            listviewChat.scrollTo(container);

            System.out.println("Displayed received message with consistent space-between layout");
        } catch (Exception e) {
            System.out.println("Error displaying received message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // handle Enter pressed action on txtFieldMessage and call the sendMessageAction
    @FXML
    private void txtFieldOnKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            sendMessageAction(new ActionEvent());
            event.consume(); // Prevent new line
        }
    }

    @FXML
    void btnSendEmailAction(ActionEvent event) {
        try {
            // Lấy thông tin hiện tại từ định dạng văn bản
            String currentFont = fontComboBox.getValue();
            String currentSize = fontSizeComboBox.getValue();
            boolean isBold = boldToggleBtn.isSelected();
            boolean isItalic = italicTogglebtn.isSelected();
            boolean isUnderline = lineToggleBtn.isSelected();
            Color currentColor = colorPicker.getValue();

            // Tạo mã màu hex
            String colorHex = String.format("#%02X%02X%02X",
                    (int) (currentColor.getRed() * 255),
                    (int) (currentColor.getGreen() * 255),
                    (int) (currentColor.getBlue() * 255));

            // Lấy email người nhận
            User receiver = clientView.getUser(this.receiver);
            if (receiver == null || receiver.getEmail() == null || receiver.getEmail().trim().isEmpty()) {
                clientView.showError("Email Error", "Missing Email",
                        "Could not send email because the recipient does not have an email address.");
                return;
            }

            try {
                // Tạo controller trước
                SendEmailSceneController emailController = new SendEmailSceneController(
                        clientView.getUserInformation().getEmail(),
                        receiver.getEmail(),
                        currentFont,
                        currentSize,
                        isBold,
                        isItalic,
                        isUnderline,
                        colorHex);

                // Tải FXML mà không đặt controller trong tệp FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("SendEmailScene.fxml"));

                // Đặt controller cho loader
                loader.setController(emailController);

                // Tải giao diện
                Parent parent = loader.load();

                // Tạo cửa sổ mới với kiểu dáng đã được định nghĩa
                Stage stage = new Stage();
                Scene scene = new Scene(parent);

                // Thêm stylesheet nếu có
                File cssFile = new File("src/resouces/chatBoxStyle.css");
                if (cssFile.exists()) {
                    scene.getStylesheets().add("file:///" + cssFile.getAbsolutePath().replace("\\", "/"));
                }

                stage.setScene(scene);
                stage.setTitle("Send Email");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ex) {
                clientView.showError("Error", "Cannot Open Email Window", "An error occurred: " + ex.getMessage());
                Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            clientView.showError("Error", "Cannot Open Email Window", "An error occurred: " + ex.getMessage());
            Logger.getLogger(ChatBoxController.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
    }

    /*
     * customize Editor pane with styles (bold,italic,font,size ..)
     */
    void customizeEditorPane() {
        try {
            // Font family setup
            ObservableList<String> limitedFonts = FXCollections.observableArrayList(
                    "Arial",
                    "Times New Roman",
                    "Courier New",
                    "Comic Sans MS",
                    "Tahoma",
                    "Verdana",
                    "Georgia",
                    "Segoe UI",
                    "Trebuchet MS",
                    "Impact");
            fontComboBox.setItems(limitedFonts);
            fontComboBox.setCellFactory(listView -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-family: \"" + item + "\";");
                    }
                }
            });
            fontComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-family: \"" + item + "\";");
                    }
                }
            });
            fontComboBox.getSelectionModel().selectFirst();

            // Font size setup with visual indicators
            ObservableList<String> fontSizes = FXCollections.observableArrayList(
                    "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "28", "32", "36", "42", "48", "72");
            fontSizeComboBox.setItems(fontSizes);
            fontSizeComboBox.setCellFactory(listView -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: " + item + "px;");
                    }
                }
            });
            fontSizeComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: " + item + "px;");
                    }
                }
            });
            fontSizeComboBox.getSelectionModel().select(4); // Default to 12

            // Color picker
            colorPicker.setValue(Color.BLACK);

            // Force the combo boxes to display their current selected value
            Platform.runLater(() -> {
                fontComboBox.setVisible(false);
                fontComboBox.setVisible(true);
                fontSizeComboBox.setVisible(false);
                fontSizeComboBox.setVisible(true);
            });

            System.out.println("Editor pane customized with fonts and sizes");
        } catch (Exception e) {
            System.out.println("Error customizing editor pane: " + e.getMessage());
            e.printStackTrace();
        }
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
        try {
            // Clear existing messages
            listviewChat.getItems().clear();

            // Sort messages chronologically
            if (messages != null && !messages.isEmpty()) {
                messages.sort((msg1, msg2) -> {
                    if (msg1.getDate() == null && msg2.getDate() == null)
                        return 0;
                    if (msg1.getDate() == null)
                        return -1;
                    if (msg2.getDate() == null)
                        return 1;
                    return msg1.getDate().compare(msg2.getDate());
                });
            } else {
                System.out.println("No messages to load or messages list is null");
                return;
            }

            System.out.println("Loading " + messages.size() + " messages in history with consistent spacing");

            for (Message message : messages) {
                try {
                    // Determine if message is from current user
                    boolean isFromCurrentUser = message.getFrom().equals(
                            clientView.getUserInformation().getUsername());

                    // Create container with proper spacing - CONSISTENT WIDTH
                    HBox container = new HBox(10);
                    container.setMaxWidth(listviewChat.getWidth());
                    container.setPrefWidth(listviewChat.getWidth() - 20);

                    // Store sender info in userData for future reference
                    container.setUserData(message.getFrom());

                    // Set alignment and padding based on sender
                    if (isFromCurrentUser) {
                        container.setAlignment(Pos.CENTER_RIGHT);
                        // IMPORTANT: Fixed right padding of 75px for consistent space-between effect
                        container.setPadding(new Insets(5, 5, 5, 75));
                    } else {
                        container.setAlignment(Pos.CENTER_LEFT);
                        // IMPORTANT: Fixed left padding of 75px for consistent space-between effect
                        container.setPadding(new Insets(5, 75, 5, 5));
                    }

                    // Create message bubble
                    VBox messageBox = new VBox(4);
                    // IMPORTANT: Limit message width for better readability
                    messageBox.setMaxWidth(250);
                    messageBox.setPadding(new Insets(8, 12, 8, 12));

                    // Style based on sender with consistent colors
                    String bubbleColor = isFromCurrentUser ? "#0084ff" : "#e4e6eb";
                    messageBox.setStyle("-fx-background-color: " + bubbleColor + "; -fx-background-radius: 15;");

                    // Add group sender name if applicable
                    if (!isFromCurrentUser && message.getTo().contains("##")) {
                        Text senderName = new Text(message.getFrom());
                        senderName.setStyle("-fx-font-size: 11px; -fx-fill: #3578e5; -fx-font-weight: bold;");
                        VBox.setMargin(senderName, new Insets(0, 0, 4, 0));
                        messageBox.getChildren().add(senderName);
                    }

                    // Create message text
                    Label messageLabel = new Label(message.getBody());
                    messageLabel.setMaxWidth(250);
                    messageLabel.setWrapText(true);

                    // Apply text formatting using the new helper method
                    applyMessageFormatting(messageLabel, message, isFromCurrentUser);

                    // Create timestamp
                    String timeStr = formatTimestampFromMessage(message);
                    Text timestamp = new Text(timeStr);
                    timestamp.setFill(isFromCurrentUser ? Color.WHITE : Color.GRAY);
                    timestamp.setOpacity(0.8);
                    timestamp.setStyle("-fx-font-size: 10px;");

                    // Add message components
                    messageBox.getChildren().addAll(messageLabel, timestamp);

                    // Create avatar
                    ImageView avatar = new ImageView();
                    avatar.setFitWidth(32);
                    avatar.setFitHeight(32);
                    // Disable caching for avatars
                    avatar.setCache(false);

                    // Get user for avatar
                    User user = isFromCurrentUser ? clientView.getUserInformation()
                            : clientView.getUser(message.getFrom());

                    // Set avatar image with cache busting
                    if (user != null) {
                        String imagePath = null;
                        if (user.getImage() != null && !user.getImage().isEmpty()) {
                            imagePath = user.getImage();
                            // Add cache busting parameter
                            if (imagePath.startsWith("http")) {
                                String separator = imagePath.contains("?") ? "&" : "?";
                                imagePath += separator + "t=" + System.currentTimeMillis();
                            }
                        } else {
                            // Set default based on gender with consistent path
                            imagePath = user.getGender() != null &&
                                    user.getGender().equalsIgnoreCase("Female")
                                            ? "/resouces/female.png"
                                            : "/resouces/male.png";
                        }
                        avatar.setImage(loadImageWithCacheBusting(imagePath));
                    } else {
                        avatar.setImage(loadImageWithCacheBusting("/resouces/male.png"));
                    }

                    // Apply circular clipping
                    makeCircularImage(avatar, 16);

                    // Add components in correct order with consistent spacing
                    if (isFromCurrentUser) {
                        container.getChildren().addAll(messageBox, avatar);
                    } else {
                        container.getChildren().addAll(avatar, messageBox);
                    }

                    // Add to ListView and scroll to it
                    listviewChat.getItems().add(container);

                } catch (Exception ex) {
                    System.out.println("Error loading message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Scroll to the last message
            if (!messages.isEmpty()) {
                Platform.runLater(() -> {
                    listviewChat.scrollTo(listviewChat.getItems().size() - 1);
                    System.out.println("Scrolled to most recent message");

                    // Always enforce consistent spacing after loading messages
                    enforceConsistentMessageLayout();
                });
            }
        } catch (Exception ex) {
            System.out.println("Error in loadHistory: " + ex.getMessage());
            ex.printStackTrace();
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
                // Get fresh user info (to ensure we have latest image)
                User friend = clientView.getUser(friendName);

                // Set user image based on gender
                if (friend != null && friend.getImage() != null) {
                    // Use cache busting to get the freshest image
                    imgFriendStatus.setImage(loadImageWithCacheBusting(friend.getImage()));
                } else {
                    String gender = clientView.getGender(friendName);
                    String defaultImagePath = gender != null && gender.equals("Female") ? "/resouces/female_32.png"
                            : "/resouces/user_32.png";
                    imgFriendStatus.setImage(loadImageWithCacheBusting(defaultImagePath));
                }

                // Make profile image circular
                makeCircularImage(imgFriendStatus, 16);

            } catch (Exception ex) {
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

    private void loadHistory() {
        History = clientView.getHistory(receiver);
        if (History != null) {
            for (Message msg : History) {
                addMessageToChat(msg);
            }
        }
    }

    private void addMessageToChat(Message message) {
        HBox messageBox = new HBox(10);
        messageBox
                .setAlignment(message.getFrom().equals(clientView.getUserInformation().getUsername()) ? Pos.CENTER_RIGHT
                        : Pos.CENTER_LEFT);

        // Add avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(32);
        avatar.setFitHeight(32);
        avatar.setPreserveRatio(true);

        User sender = clientView.getUser(message.getFrom());
        if (sender != null && sender.getImage() != null) {
            avatar.setImage(loadImageWithCacheBusting(sender.getImage()));
        } else {
            String defaultImage = sender != null && sender.getGender().equals("male") ? "/resouces/male.png"
                    : "/resouces/female.png";
            avatar.setImage(loadImageWithCacheBusting(defaultImage));
        }

        // Create message bubble
        VBox messageBubble = new VBox(5);
        messageBubble.setStyle("-fx-background-color: " +
                (message.getFrom().equals(clientView.getUserInformation().getUsername()) ? "#0084ff" : "#e4e6eb")
                + "; -fx-background-radius: 15; -fx-padding: 8;");

        Text messageText = new Text(message.getBody());
        messageText.setFill(
                message.getFrom().equals(clientView.getUserInformation().getUsername()) ? Color.WHITE : Color.BLACK);

        Text timeText = new Text(formatTimestampFromMessage(message));
        timeText.setFill(
                message.getFrom().equals(clientView.getUserInformation().getUsername()) ? Color.WHITE : Color.GRAY);
        timeText.setStyle("-fx-font-size: 10px;");

        messageBubble.getChildren().addAll(messageText, timeText);

        // Add components to message box
        if (message.getFrom().equals(clientView.getUserInformation().getUsername())) {
            messageBox.getChildren().addAll(messageBubble, avatar);
        } else {
            messageBox.getChildren().addAll(avatar, messageBubble);
        }

        listviewChat.getItems().add(messageBox);
        listviewChat.scrollTo(listviewChat.getItems().size() - 1);
    }

    @FXML
    private void toggleBoldAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void toggleItalicAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void toggleUnderlineAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void changeFontAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void changeColorAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void changeFontSizeAction(ActionEvent event) {
        updateTextAreaPreview();
    }

    @FXML
    private void clearFormatAction(ActionEvent event) {
        // Reset all formatting controls to default values
        boldToggleBtn.setSelected(false);
        italicTogglebtn.setSelected(false);
        lineToggleBtn.setSelected(false);
        fontComboBox.setValue("System");
        fontSizeComboBox.setValue("12");
        colorPicker.setValue(Color.BLACK);

        // Apply the default style to the text area
        updateTextAreaPreview();
    }

    /**
     * Load image with cache busting to ensure fresh avatars
     * 
     * @param imagePath The path to the image
     * @return Image object with the loaded image
     */
    private Image loadImageWithCacheBusting(String imagePath) {
        try {
            // If the path is null or empty, return default image
            if (imagePath == null || imagePath.trim().isEmpty()) {
                return new Image(getClass().getResource("/resouces/user_32.png").toExternalForm());
            }

            // Add current timestamp to URL to prevent caching
            String timeStamp = "?t=" + System.currentTimeMillis();

            // Handle different types of paths
            if (imagePath.startsWith("http")) {
                // For URLs, add timestamp parameter
                String separator = imagePath.contains("?") ? "&" : "?";
                return new Image(imagePath + separator + "t=" + System.currentTimeMillis());
            } else if (imagePath.startsWith("file:")) {
                // For file URLs, directly use the path
                return new Image(imagePath);
            } else if (imagePath.startsWith("/")) {
                // For resource paths, use resource URL
                return new Image(getClass().getResource(imagePath).toExternalForm());
            } else {
                // Default case - try as-is
                return new Image(imagePath);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + imagePath + ", " + e.getMessage());
            // Return default image if loading fails
            try {
                return new Image(getClass().getResource("/resouces/user_32.png").toExternalForm());
            } catch (Exception ex) {
                // Last resort default
                return null;
            }
        }
    }

    /**
     * Create simple circular clipping for avatar images
     */
    private void makeCircularImage(ImageView imageView, double radius) {
        if (imageView != null) {
            // Calculate center point based on image dimensions
            double centerX = imageView.getFitWidth() / 2;
            double centerY = imageView.getFitHeight() / 2;

            // Create a perfect circle clip
            Circle clip = new Circle(centerX, centerY, radius);
            imageView.setClip(clip);

            // Add style class for consistent styling
            imageView.getStyleClass().add("image-view");
        }
    }

    /**
     * Updates the text area with current formatting settings
     */
    private void updateTextAreaPreview() {
        try {
            // Save current caret position
            int caretPosition = txtFieldMsg.getCaretPosition();

            // Get current formatting options with null checks
            String fontFamily = fontComboBox.getValue();
            if (fontFamily == null) {
                fontFamily = "Arial";
            }

            String fontSize = fontSizeComboBox.getValue();
            if (fontSize == null) {
                fontSize = "12";
            }

            String fontWeight = boldToggleBtn.isSelected() ? "bold" : "normal";
            String fontStyle = italicTogglebtn.isSelected() ? "italic" : "normal";
            String textDecoration = lineToggleBtn.isSelected() ? "underline" : "none";

            // Get color as hex string with null check
            Color color = colorPicker.getValue();
            if (color == null) {
                color = Color.BLACK;
            }

            String colorHex = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            // Build the style string
            StringBuilder styleBuilder = new StringBuilder();
            styleBuilder.append("-fx-font-family: \"").append(fontFamily).append("\"; ");
            styleBuilder.append("-fx-font-size: ").append(fontSize).append("px; ");
            styleBuilder.append("-fx-font-weight: ").append(fontWeight).append("; ");
            styleBuilder.append("-fx-font-style: ").append(fontStyle).append("; ");
            styleBuilder.append("-fx-text-fill: ").append(colorHex).append("; ");

            if (!"none".equals(textDecoration)) {
                styleBuilder.append("-fx-underline: true; ");
            }

            String style = styleBuilder.toString();

            // Apply style to text area
            txtFieldMsg.setStyle(style);

            // Restore caret position
            txtFieldMsg.positionCaret(caretPosition);

            // Log the applied style for debugging
            System.out.println("Applied style: " + style);
        } catch (Exception e) {
            System.out.println("Error updating text area preview: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Apply message formatting to a Label
     * 
     * @param label      The label to apply formatting to
     * @param message    The message containing formatting information
     * @param isSentByMe Whether this is a message sent by the current user
     */
    private void applyMessageFormatting(Label label, Message message, boolean isSentByMe) {
        StringBuilder styleBuilder = new StringBuilder();

        // Font weight
        styleBuilder.append("-fx-font-weight: ").append(message.getFontWeight()).append("; ");

        // Font size
        styleBuilder.append("-fx-font-size: ").append(message.getFontsSize()).append("px; ");

        // Font style
        styleBuilder.append("-fx-font-style: ").append(message.getFontStyle()).append("; ");

        // Font family
        styleBuilder.append("-fx-font-family: \"").append(message.getFontFamily()).append("\"; ");

        // Underline
        styleBuilder.append("-fx-underline: ").append(message.getUnderline()).append("; ");

        // Color - only for received messages, sent messages use white by default
        if (!isSentByMe && message.getFontColor() != null) {
            styleBuilder.append("-fx-text-fill: ").append(message.getFontColor()).append(";");
        }

        label.setStyle(styleBuilder.toString());

        // Set text color separately if this is a sent message
        if (isSentByMe) {
            label.setTextFill(Color.WHITE);
        }
    }

    /**
     * Appends a message to the chat history box
     * 
     * @param message    The message to add
     * @param cssPath    Path to CSS file for styling
     * @param isFirstMsg Flag to indicate if this is the first message in a sequence
     */
    private void appendToHistoryBox(Message message, String cssPath, Boolean isFirstMsg) {
        try {
            Platform.runLater(() -> {
                try {
                    // Create message container with proper spacing
                    boolean isSentByMe = message.getFrom().equals(clientView.getUserInformation().getUsername());
                    HBox container = new HBox(10);
                    container.setMaxWidth(listviewChat.getWidth());
                    container.setPrefWidth(listviewChat.getWidth() - 20);

                    // Apply alignment based on sender
                    if (isSentByMe) {
                        container.setAlignment(Pos.CENTER_RIGHT);
                        container.setPadding(new Insets(5, 5, 5, 75)); // Left padding for sent messages
                    } else {
                        container.setAlignment(Pos.CENTER_LEFT);
                        container.setPadding(new Insets(5, 75, 5, 5)); // Right padding for received messages
                    }

                    // Create avatar image
                    ImageView avatar = new ImageView();
                    avatar.setFitWidth(32);
                    avatar.setFitHeight(32);
                    avatar.setCache(false);

                    // Get user avatar based on sender
                    User user = isSentByMe ? clientView.getUserInformation() : clientView.getUser(message.getFrom());
                    if (user != null) {
                        if (user.getImage() != null && !user.getImage().isEmpty()) {
                            avatar.setImage(loadImageWithCacheBusting(user.getImage()));
                        } else {
                            // Default avatar based on gender
                            String defaultImage = user.getGender() != null &&
                                    user.getGender().equalsIgnoreCase("Female")
                                            ? "/resouces/female.png"
                                            : "/resouces/male.png";
                            avatar.setImage(loadImageWithCacheBusting(defaultImage));
                        }
                    } else {
                        // Fallback default avatar
                        avatar.setImage(loadImageWithCacheBusting("/resouces/user_32.png"));
                    }

                    // Make avatar circular
                    makeCircularImage(avatar, 16);

                    // Create message bubble
                    VBox messageBox = new VBox(4);
                    messageBox.setMaxWidth(250);
                    messageBox.setPadding(new Insets(8, 12, 8, 12));

                    // Style based on sender
                    String bubbleColor = isSentByMe ? "#0084ff" : "#e4e6eb";
                    messageBox.setStyle("-fx-background-color: " + bubbleColor + "; -fx-background-radius: 15;");

                    // Create message label with styling
                    Label messageLabel = new Label(message.getBody());
                    messageLabel.setMaxWidth(250);
                    messageLabel.setWrapText(true);

                    // Apply message formatting from message properties
                    applyMessageFormatting(messageLabel, message, isSentByMe);

                    // Add timestamp
                    String timeStr = formatTimestampFromMessage(message);
                    Text timestamp = new Text(timeStr);
                    timestamp.setFill(isSentByMe ? Color.WHITE : Color.GRAY);
                    timestamp.setOpacity(0.8);
                    timestamp.setStyle("-fx-font-size: 10px;");

                    // Add components to message box
                    messageBox.getChildren().addAll(messageLabel, timestamp);

                    // Add components in correct order based on sender
                    if (isSentByMe) {
                        container.getChildren().addAll(messageBox, avatar);
                    } else {
                        container.getChildren().addAll(avatar, messageBox);
                    }

                    // Add to ListView and scroll to it
                    listviewChat.getItems().add(container);
                    listviewChat.scrollTo(container);

                    // Force consistent layout
                    enforceConsistentMessageLayout();

                } catch (Exception ex) {
                    System.out.println("Error appending message to history: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            System.out.println("Error in appendToHistoryBox: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Update avatar displayed in chat
     * 
     * @param username The username whose avatar needs updating
     */
    public void refreshUserAvatar(String username) {
        // Refresh the friend avatar in header if it's the current chat
        if (username.equals(receiver) && imgFriendStatus != null) {
            Platform.runLater(() -> {
                User friend = clientView.getUser(username);
                if (friend != null) {
                    if (friend.getImage() != null) {
                        imgFriendStatus.setImage(loadImageWithCacheBusting(friend.getImage()));
                    } else {
                        String defaultImage = friend.getGender().equals("male") ? "/resouces/male.png"
                                : "/resouces/female.png";
                        imgFriendStatus.setImage(loadImageWithCacheBusting(defaultImage));
                    }
                    makeCircularImage(imgFriendStatus, 16);
                }
            });
        }

        // Force reload of all messages in history to refresh avatars
        if (listviewChat != null && clientView != null) {
            Platform.runLater(() -> {
                ArrayList<Message> history = clientView.getHistory(receiver);
                if (history != null && !history.isEmpty()) {
                    loadHistory(history);
                }
            });
        }
    }

    /**
     * Refresh avatar for the current user in this chat
     */
    public void refreshCurrentUserAvatar() {
        try {
            Platform.runLater(() -> {
                try {
                    // Get current user information
                    User currentUser = clientView.getUserInformation();
                    if (currentUser == null)
                        return;

                    System.out.println("Refreshing current user avatar in chat with: " + receiver);

                    // Find all sent messages by the current user
                    for (int i = 0; i < listviewChat.getItems().size(); i++) {
                        HBox container = listviewChat.getItems().get(i);

                        // Skip if empty
                        if (container == null || container.getChildren().isEmpty()) {
                            continue;
                        }

                        // Check if this is a sent message (avatar on right)
                        // In sent messages, avatar is the last child
                        if (container.getAlignment() == Pos.CENTER_RIGHT) {
                            // Get the last child which should be the avatar
                            Node lastNode = container.getChildren().get(container.getChildren().size() - 1);
                            if (lastNode instanceof ImageView) {
                                ImageView avatar = (ImageView) lastNode;

                                // Update avatar with cache busting
                                if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                                    avatar.setImage(loadImageWithCacheBusting(currentUser.getImage()));
                                } else {
                                    // Set default based on gender
                                    String defaultImage = currentUser.getGender() != null &&
                                            currentUser.getGender().equalsIgnoreCase("Female") ? "/resouces/female.png"
                                                    : "/resouces/male.png";
                                    avatar.setImage(loadImageWithCacheBusting(defaultImage));
                                }

                                // Re-apply circular clipping
                                makeCircularImage(avatar, 16);

                                // Debug log
                                System.out.println("Updated avatar in message " + i);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error refreshing current user avatar: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            System.out.println("Error in refreshCurrentUserAvatar: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Refreshes all message bubbles that contain avatars for a specific user
     * This is called when a user's avatar is updated to ensure all instances are
     * refreshed
     * 
     * @param username The username whose avatar needs to be refreshed in messages
     */
    public void refreshMessageAvatars(String username) {
        Platform.runLater(() -> {
            try {
                System.out.println("Refreshing message avatars for user: " + username);

                // Track if we found any messages to update
                boolean foundMessages = false;

                // Iterate through all message bubbles in the chat view
                if (chatBox != null) {
                    for (Node node : chatBox.getChildren()) {
                        // Check if this is a message bubble HBox
                        if (node instanceof HBox) {
                            HBox messageBox = (HBox) node;

                            // Look for ImageViews that might be avatars
                            for (Node child : messageBox.getChildren()) {
                                // Check for avatar containers that could be stacks or direct ImageViews
                                if (child instanceof StackPane || child instanceof ImageView) {
                                    // Get potential user information from userData property
                                    Object userData = messageBox.getUserData();

                                    if (userData instanceof String) {
                                        String messageUserData = (String) userData;

                                        // Check if this message belongs to the target user
                                        if (messageUserData.contains(username)) {
                                            foundMessages = true;

                                            // Force reload the avatar with cache busting
                                            ImageView avatar = null;

                                            // Extract ImageView from either StackPane or direct reference
                                            if (child instanceof StackPane) {
                                                StackPane stackPane = (StackPane) child;
                                                // Find ImageView inside stack pane
                                                for (Node stackChild : stackPane.getChildren()) {
                                                    if (stackChild instanceof ImageView) {
                                                        avatar = (ImageView) stackChild;
                                                        break;
                                                    }
                                                }
                                            } else if (child instanceof ImageView) {
                                                avatar = (ImageView) child;
                                            }

                                            // If we found an avatar, refresh it
                                            if (avatar != null) {
                                                // Get fresh user information
                                                User user = clientView.getUser(username);

                                                if (user != null) {
                                                    // Disable caching
                                                    avatar.setCache(false);

                                                    // Load appropriate image with cache busting
                                                    if (user.getImage() != null && !user.getImage().trim().isEmpty()) {
                                                        String imagePath = user.getImage();
                                                        if (imagePath.startsWith("http")) {
                                                            // Add timestamp for cache busting
                                                            String separator = imagePath.contains("?") ? "&" : "?";
                                                            imagePath += separator + "t=" + System.currentTimeMillis();
                                                        }
                                                        avatar.setImage(new Image(imagePath, true));
                                                    } else {
                                                        // Load default image based on gender
                                                        String defaultImage = (user.getGender() != null &&
                                                                user.getGender().equalsIgnoreCase("Female"))
                                                                        ? "/resouces/female.png"
                                                                        : "/resouces/user.png";

                                                        String fullPath = getClass().getResource(defaultImage)
                                                                .toExternalForm() +
                                                                "?t=" + System.currentTimeMillis();

                                                        avatar.setImage(new Image(fullPath, true));
                                                    }

                                                    // Make sure it's circular
                                                    makeCircularImage(avatar, 20);

                                                    System.out.println(
                                                            "Updated avatar in message bubble for: " + username);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!foundMessages) {
                    System.out.println("No message bubbles found for user: " + username);
                }
            } catch (Exception e) {
                System.out.println("Error refreshing message avatars: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Ensures all messages in the chat display have consistent spacing
     * Call this after loading history or when UI issues are detected
     */
    public void enforceConsistentMessageLayout() {
        Platform.runLater(() -> {
            try {
                System.out.println(
                        "Enforcing consistent message spacing for " + listviewChat.getItems().size() + " messages");

                // First update all container settings
                for (HBox container : listviewChat.getItems()) {
                    if (container == null)
                        continue;

                    // Ensure correct width
                    container.setPrefWidth(listviewChat.getWidth() - 20);
                    container.setMaxWidth(listviewChat.getWidth() - 20);

                    // Apply spacing based on alignment (sender)
                    if (container.getAlignment() == Pos.CENTER_RIGHT) {
                        // Sent messages: padding on left side
                        container.setPadding(new Insets(5, 5, 5, 75));
                    } else {
                        // Received messages: padding on right side
                        container.setPadding(new Insets(5, 75, 5, 5));
                    }

                    // Ensure message bubbles have consistent width
                    for (Node child : container.getChildren()) {
                        if (child instanceof VBox) {
                            VBox messageBox = (VBox) child;
                            messageBox.setMaxWidth(250);

                            // Make sure child nodes in bubble are properly styled
                            for (Node bubbleChild : messageBox.getChildren()) {
                                if (bubbleChild instanceof Label) {
                                    Label label = (Label) bubbleChild;
                                    label.setMaxWidth(250);
                                    label.setWrapText(true);
                                }
                            }
                        } else if (child instanceof ImageView) {
                            // Ensure avatar has consistent size
                            ImageView avatar = (ImageView) child;
                            avatar.setFitWidth(32);
                            avatar.setFitHeight(32);

                            // Make circular if not already
                            if (avatar.getClip() == null) {
                                makeCircularImage(avatar, 16);
                            }
                        }
                    }
                }

                // Force refresh the list view
                listviewChat.refresh();

                System.out.println("Message layout consistency enforced");
            } catch (Exception e) {
                System.out.println("Error enforcing message layout: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Set up event handlers for formatting controls
     */
    private void setupFormattingControls() {
        // Toggle buttons
        boldToggleBtn.setOnAction(event -> {
            updateTextAreaPreview();
        });

        italicTogglebtn.setOnAction(event -> {
            updateTextAreaPreview();
        });

        lineToggleBtn.setOnAction(event -> {
            updateTextAreaPreview();
        });

        // ComboBoxes
        fontComboBox.setOnAction(event -> {
            updateTextAreaPreview();
        });

        fontSizeComboBox.setOnAction(event -> {
            updateTextAreaPreview();
        });

        // ColorPicker
        colorPicker.setOnAction(event -> {
            updateTextAreaPreview();
        });

        // Keyboard shortcuts
        txtFieldMsg.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.B) {
                    boldToggleBtn.setSelected(!boldToggleBtn.isSelected());
                    updateTextAreaPreview();
                    event.consume();
                } else if (event.getCode() == KeyCode.I) {
                    italicTogglebtn.setSelected(!italicTogglebtn.isSelected());
                    updateTextAreaPreview();
                    event.consume();
                } else if (event.getCode() == KeyCode.U) {
                    lineToggleBtn.setSelected(!lineToggleBtn.isSelected());
                    updateTextAreaPreview();
                    event.consume();
                }
            }
        });
    }

    /**
     * Set the receiver for this chat box when loaded from FXML
     * This method is called from ChatSceneController after loading FXML
     * 
     * @param receiver the username of the chat recipient
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;

        // Initialize friend info now that we have the receiver
        User friend = clientView.getUser(receiver);
        if (friend != null) {
            labelFriendName.setText(friend.getFname() + " " + friend.getLname());
            labelFriendStatus.setText(friend.getStatus());

            try {
                // Set friend's avatar with forced reload - safely
                Image image;
                if (friend.getImage() != null && !friend.getImage().trim().isEmpty()) {
                    image = loadImageWithCacheBusting(friend.getImage());
                } else {
                    // Set default avatar based on gender
                    String defaultImage = friend.getGender() != null && friend.getGender().equalsIgnoreCase("Female")
                            ? "/resouces/female.png"
                            : "/resouces/male.png";
                    image = loadImageWithCacheBusting(defaultImage);
                }

                // Set image and make it circular
                imgFriendStatus.setImage(image);
                imgFriendStatus.setFitWidth(32);
                imgFriendStatus.setFitHeight(32);
                imgFriendStatus.setPreserveRatio(true);
                makeCircularImage(imgFriendStatus, 16);
            } catch (Exception ex) {
                System.out.println("Error loading friend avatar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // Load chat history if available
        if (clientView.getHistory(receiver) != null) {
            loadHistory(clientView.getHistory(receiver));
            enforceConsistentMessageLayout();
        } else {
            loadHistory();
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút emoji
     * 
     * @param event
     */
    @FXML
    private void btnEmojiAction(ActionEvent event) {
        try {
            // Tạo cửa sổ popup cho emoji picker
            Stage emojiStage = new Stage();
            emojiStage.setTitle("Chọn Emoji");
            emojiStage.initModality(Modality.WINDOW_MODAL);
            emojiStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            // Tạo GridPane để hiển thị các emoji
            javafx.scene.layout.GridPane gridPane = new javafx.scene.layout.GridPane();
            gridPane.setPadding(new Insets(10));
            gridPane.setHgap(10);
            gridPane.setVgap(10);

            // Danh sách emoji cơ bản
            String[] emojis = {
                    "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆",
                    "😉", "😊", "😋", "😎", "😍", "😘", "😗", "😙",
                    "😚", "🙂", "🤗", "🤔", "😐", "😑", "😶", "🙄",
                    "😏", "😣", "😥", "😮", "😯", "😪", "😫", "😴",
                    "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔",
                    "😕", "🙃", "🤑", "😲", "☹️", "🙁", "😖", "😞",
                    "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩",
                    "😬", "😰", "😱", "😳", "😵", "😡", "😠", "❤️",
                    "👍", "👎", "👏", "🙌", "👐", "🤝", "🙏", "✌️",
                    "🤞", "🤘", "👌", "👈", "👉", "👆", "👇", "✋"
            };

            int col = 0;
            int row = 0;
            int maxCol = 8; // 8 emoji mỗi hàng

            for (String emoji : emojis) {
                Button btnEmoji = new Button(emoji);
                btnEmoji.setStyle("-fx-font-size: 18px; -fx-padding: 5px; -fx-background-color: transparent;");
                btnEmoji.setPrefSize(40, 40);

                // Add hover effect
                btnEmoji.setOnMouseEntered(e -> {
                    btnEmoji.setStyle("-fx-font-size: 20px; -fx-padding: 5px; -fx-background-color: #f0f0f0;");
                });

                btnEmoji.setOnMouseExited(e -> {
                    btnEmoji.setStyle("-fx-font-size: 18px; -fx-padding: 5px; -fx-background-color: transparent;");
                });

                // Thêm sự kiện khi nhấn vào emoji - ensure formatting is preserved
                btnEmoji.setOnAction(e -> {
                    try {
                        // Chèn emoji vào vị trí con trỏ
                        int caretPosition = txtFieldMsg.getCaretPosition();
                        String currentText = txtFieldMsg.getText();
                        String newText = currentText.substring(0, caretPosition) +
                                btnEmoji.getText() +
                                currentText.substring(caretPosition);

                        // Save current styling
                        String currentStyle = txtFieldMsg.getStyle();

                        // Update text
                        txtFieldMsg.setText(newText);

                        // Restore styling - important for emojis to work with formatting
                        txtFieldMsg.setStyle(currentStyle);

                        // Đóng cửa sổ emoji
                        emojiStage.close();

                        // Đặt focus trở lại ô nhập liệu
                        txtFieldMsg.requestFocus();
                        txtFieldMsg.positionCaret(caretPosition + btnEmoji.getText().length());
                    } catch (Exception ex) {
                        System.out.println("Error inserting emoji: " + ex.getMessage());
                    }
                });

                gridPane.add(btnEmoji, col, row);

                col++;
                if (col >= maxCol) {
                    col = 0;
                    row++;
                }
            }

            // Tạo ScrollPane để cuộn nếu có quá nhiều emoji
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(gridPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(300);
            scrollPane.setStyle("-fx-background-color: white;");

            // Add a search field
            TextField searchField = new TextField();
            searchField.setPromptText("Tìm kiếm emoji...");
            searchField.setPrefHeight(30);
            searchField.setStyle("-fx-border-radius: 15; -fx-background-radius: 15;");

            // Tạo VBox để chứa thanh tìm kiếm và các emoji
            VBox vbox = new VBox(10, searchField, scrollPane);
            vbox.setPadding(new Insets(10));
            vbox.setStyle("-fx-background-color: white;");

            Scene scene = new Scene(vbox);
            emojiStage.setScene(scene);
            emojiStage.setResizable(false);

            // Thiết lập vị trí hiển thị popup
            Button source = (Button) event.getSource();
            Point2D point = source.localToScreen(0, 0);
            emojiStage.setX(point.getX());
            emojiStage.setY(point.getY() - 350); // Hiển thị phía trên nút

            emojiStage.show();
        } catch (Exception e) {
            System.out.println("Error showing emoji picker: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a recording indicator that shows when recording is in progress
     */
    private void createRecordingIndicator() {
        recordingTimeLabel = new Label("0:00");
        recordingTimeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Circle recordingDot = new Circle(5);
        recordingDot.setFill(Color.RED);

        recordingIndicator = new HBox(10);
        recordingIndicator.setAlignment(Pos.CENTER);
        recordingIndicator.getChildren().addAll(recordingDot, recordingTimeLabel, new Label("Recording..."));
        recordingIndicator.setStyle("-fx-background-color: #ffeeee; -fx-padding: 5px; -fx-background-radius: 5px;");
        recordingIndicator.setVisible(false);
    }

    /**
     * Handle voice recording button click
     */
    @FXML
    private void btnVoiceRecordAction(ActionEvent event) {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecordingAndSend();
        }
    }

    /**
     * Start voice recording
     */
    private void startRecording() {
        try {
            // Create temporary file path for recording
            String tempDir = System.getProperty("java.io.tmpdir");
            recordingTempPath = tempDir + "/voicemsg_" + System.currentTimeMillis() + ".wav";

            // Start recording
            audioRecorder.startRecording(recordingTempPath);
            isRecording = true;
            recordingDuration = 0;

            // Update UI to show recording in progress
            updateRecordingButtonAppearance(true);

            // Show recording indicator at the top of the message list
            if (!listviewChat.getItems().contains(recordingIndicator)) {
                HBox indicatorContainer = new HBox(recordingIndicator);
                indicatorContainer.setAlignment(Pos.CENTER);
                listviewChat.getItems().add(indicatorContainer);
                listviewChat.scrollTo(listviewChat.getItems().size() - 1);
            }
            recordingIndicator.setVisible(true);

            // Start timer to show recording duration
            recordingTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                recordingDuration++;
                int minutes = recordingDuration / 60;
                int seconds = recordingDuration % 60;
                recordingTimeLabel.setText(String.format("%d:%02d", minutes, seconds));

                // Limit recording time to 2 minutes (120 seconds)
                if (recordingDuration >= 120) {
                    stopRecordingAndSend();
                }
            }));
            recordingTimer.setCycleCount(Timeline.INDEFINITE);
            recordingTimer.play();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Recording Error", "Could not start recording: " + e.getMessage());
        }
    }

    /**
     * Stop recording and send the voice message
     */
    private void stopRecordingAndSend() {
        try {
            if (recordingTimer != null) {
                recordingTimer.stop();
            }

            // Stop recording
            audioRecorder.stopRecording();
            isRecording = false;

            // Update UI
            updateRecordingButtonAppearance(false);
            recordingIndicator.setVisible(false);

            // Remove recording indicator if it exists
            listviewChat.getItems().removeIf(item -> item.getChildren().contains(recordingIndicator));

            // Read audio data from file
            byte[] audioData = audioRecorder.getAudioData();

            // Check if we have valid audio data
            if (audioData != null && audioData.length > 0) {
                // Create and send voice message
                VoiceMessage voiceMessage = createVoiceMessage(audioData, audioRecorder.getRecordingDuration());
                sendVoiceMessage(voiceMessage);
            } else {
                showAlert("Recording Error", "No audio data was recorded.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Recording Error", "Error sending voice message: " + e.getMessage());
        }
    }

    /**
     * Create a VoiceMessage object from recorded audio
     */
    private VoiceMessage createVoiceMessage(byte[] audioData, int duration) throws DatatypeConfigurationException {
        try {
            // Create XML calendar for the message timestamp
            GregorianCalendar calendar = new GregorianCalendar();
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

            // Get current formatting options from UI controls with null checks
            String fontFamily = fontComboBox.getValue();
            if (fontFamily == null) {
                fontFamily = "Arial";
            }

            String fontSize = fontSizeComboBox.getValue();
            if (fontSize == null) {
                fontSize = "12";
            }

            Boolean isBold = boldToggleBtn.isSelected();
            Boolean isItalic = italicTogglebtn.isSelected();
            Boolean isUnderlined = lineToggleBtn.isSelected();

            // Get color as hex with null check
            Color color = colorPicker.getValue();
            if (color == null) {
                color = Color.BLACK;
            }

            String colorHex = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            // Create voice message with current formatting
            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.setFrom(clientView.getUserInformation().getUsername());
            voiceMessage.setTo(receiver);
            voiceMessage.setDate(xmlCalendar);

            // Safe parsing of font size
            int fontSizeInt = 12; // Default value
            try {
                fontSizeInt = Integer.parseInt(fontSize);
            } catch (NumberFormatException e) {
                System.out.println("Invalid font size format in voice message, using default: " + e.getMessage());
            }
            voiceMessage.setFontsSize(fontSizeInt);

            voiceMessage.setFontColor(colorHex);
            voiceMessage.setFontFamily(fontFamily);
            voiceMessage.setFontStyle(isItalic ? "italic" : "normal");
            voiceMessage.setFontWeight(isBold ? "bold" : "normal");
            voiceMessage.setUnderline(isUnderlined);
            voiceMessage.setBody("Voice message (" + formatDuration(duration) + ")");
            voiceMessage.setAudioData(audioData);
            voiceMessage.setDuration(duration);
            voiceMessage.setFileExtension("wav");

            return voiceMessage;
        } catch (Exception e) {
            System.out.println("Error creating voice message: " + e.getMessage());
            e.printStackTrace();

            // Create a basic voice message as fallback
            VoiceMessage basicVoiceMessage = new VoiceMessage();
            basicVoiceMessage.setFrom(clientView.getUserInformation().getUsername());
            basicVoiceMessage.setTo(receiver);
            basicVoiceMessage.setFontsSize(12);
            basicVoiceMessage.setFontColor("#000000");
            basicVoiceMessage.setFontFamily("Arial");
            basicVoiceMessage.setFontStyle("normal");
            basicVoiceMessage.setFontWeight("normal");
            basicVoiceMessage.setUnderline(false);
            basicVoiceMessage.setBody("Voice message (" + formatDuration(duration) + ")");
            basicVoiceMessage.setAudioData(audioData);
            basicVoiceMessage.setDuration(duration);
            basicVoiceMessage.setFileExtension("wav");
            return basicVoiceMessage;
        }
    }

    /**
     * Send a voice message to the receiver
     */
    private void sendVoiceMessage(VoiceMessage voiceMessage) {
        try {
            // Display message in UI immediately for better user experience
            displaySentVoiceMessage(voiceMessage);

            // Try to send the message
            try {
                clientView.sendVoiceMessage(voiceMessage);
            } catch (RemoteException ex) {
                String errorMsg = ex.getMessage();

                // Check if this is our "offline handled" case
                if (errorMsg != null && errorMsg.contains("offline") && errorMsg.contains("stored")) {
                    // Show a friendly "message queued" notification
                    Platform.runLater(() -> {
                        HBox statusContainer = new HBox();
                        statusContainer.setAlignment(Pos.CENTER);
                        statusContainer.setPadding(new Insets(5, 10, 5, 10));

                        Label statusLabel = new Label("Voice message will be delivered when " +
                                voiceMessage.getTo() + " comes online");
                        statusLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic; -fx-font-size: 11px;");

                        // Add status icon
                        ImageView pendingIcon = new ImageView(
                                new Image(getClass().getResourceAsStream("/resouces/pending.png")));
                        pendingIcon.setFitWidth(12);
                        pendingIcon.setFitHeight(12);

                        statusContainer.getChildren().addAll(pendingIcon, statusLabel);
                        statusContainer.setSpacing(5);

                        listviewChat.getItems().add(statusContainer);
                        listviewChat.scrollTo(listviewChat.getItems().size() - 1);
                    });
                } else {
                    // For other errors, show an alert
                    showAlert("Error", "Could not send voice message: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Could not process voice message: " + ex.getMessage());
        }
    }

    /**
     * Display a sent voice message in the chat
     */
    private void displaySentVoiceMessage(VoiceMessage voiceMessage) {
        Platform.runLater(() -> {
            try {
                // Create voice message component
                VoiceMessageComponent voiceMessageComponent = new VoiceMessageComponent(voiceMessage, true);

                // Add sender info (timestamp, name, etc.)
                HBox messageContainer = new HBox();
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.setPadding(new Insets(5, 15, 5, 10));

                // Add timestamp
                VBox timestampContainer = new VBox();
                timestampContainer.setAlignment(Pos.CENTER);
                Label timestampLabel = new Label(formatTimestampFromMessage(voiceMessage));
                timestampLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 9px;");
                timestampContainer.getChildren().add(timestampLabel);

                // Combine components
                messageContainer.getChildren().addAll(timestampContainer, voiceMessageComponent);

                // Add to chat
                listviewChat.getItems().add(messageContainer);
                listviewChat.scrollTo(listviewChat.getItems().size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle receiving a voice message
     */
    public void receiveVoiceMessage(VoiceMessage voiceMessage) {
        Platform.runLater(() -> {
            try {
                // Create voice message component
                VoiceMessageComponent voiceMessageComponent = new VoiceMessageComponent(voiceMessage, false);

                // Add sender info
                HBox messageContainer = new HBox();
                messageContainer.setAlignment(Pos.CENTER_LEFT);
                messageContainer.setPadding(new Insets(5, 10, 5, 15));

                // Get and display sender's avatar
                ImageView senderAvatar = new ImageView();
                senderAvatar.setFitHeight(30);
                senderAvatar.setFitWidth(30);

                // Try to get the user's image
                User sender = clientView.getUser(voiceMessage.getFrom());
                if (sender != null) {
                    try {
                        if (sender.getImage() != null && !sender.getImage().isEmpty()) {
                            Image image = new Image(sender.getImage());
                            senderAvatar.setImage(image);
                        } else {
                            // Set default avatar based on gender
                            String defaultImage = (sender.getGender() != null &&
                                    sender.getGender().equalsIgnoreCase("Female")) ? "/resouces/female.png"
                                            : "/resouces/male.png";
                            senderAvatar.setImage(new Image(getClass().getResourceAsStream(defaultImage)));
                        }

                        // Make image circular
                        Circle clip = new Circle(15, 15, 15);
                        senderAvatar.setClip(clip);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Add sender info and timestamp
                VBox senderInfo = new VBox(3);
                Label nameLabel = new Label(voiceMessage.getFrom());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;");
                Label timestampLabel = new Label(formatTimestampFromMessage(voiceMessage));
                timestampLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 9px;");
                senderInfo.getChildren().addAll(nameLabel, timestampLabel);

                // Combine components
                HBox avatarAndInfo = new HBox(5);
                avatarAndInfo.getChildren().addAll(senderAvatar, senderInfo);
                avatarAndInfo.setAlignment(Pos.TOP_LEFT);

                VBox messageContent = new VBox(5);
                messageContent.getChildren().addAll(avatarAndInfo, voiceMessageComponent);

                messageContainer.getChildren().add(messageContent);

                // Add to chat
                listviewChat.getItems().add(messageContainer);
                listviewChat.scrollTo(listviewChat.getItems().size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Update the recording button appearance based on recording state
     */
    private void updateRecordingButtonAppearance(boolean isRecording) {
        if (isRecording) {
            btnVoiceRecord.setStyle("-fx-background-color: #ff4444; -fx-border-color: #cc0000; -fx-border-radius: 15;");
        } else {
            btnVoiceRecord
                    .setStyle("-fx-background-color: transparent; -fx-border-color: #dddddd; -fx-border-radius: 15;");
        }
    }

    /**
     * Format duration in seconds to MM:SS format
     */
    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /**
     * Show an alert dialog with the specified title and message
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Cleanup resources when the chat is closed
     */
    public void cleanup() {
        // Stop recording if it's in progress
        if (isRecording) {
            audioRecorder.stopRecording();
            isRecording = false;
            if (recordingTimer != null) {
                recordingTimer.stop();
            }
        }

        // Clean up temporary files
        if (recordingTempPath != null) {
            try {
                Files.deleteIfExists(Paths.get(recordingTempPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Cleanup any voice message components in the list
        for (HBox item : listviewChat.getItems()) {
            if (item.getChildren().stream().anyMatch(node -> node instanceof VoiceMessageComponent)) {
                ((VoiceMessageComponent) item.getChildren().stream()
                        .filter(node -> node instanceof VoiceMessageComponent)
                        .findFirst().orElse(null)).cleanup();
            }
        }
    }

    public void updateFriendStatus(String username, String status) {
        if (username.equals(receiver)) {
            Platform.runLater(() -> {
                if (labelFriendStatus != null) {
                    // Create a Circle status indicator
                    Circle statusIndicator = new Circle(5);
                    statusIndicator.setTranslateY(0);

                    switch (status.toLowerCase()) {
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
            });
        }
    }

    public void initializeWithMessage(Message message) {
        this.message = message;
        // Initialize other necessary components
        if (message.getTo().contains("##")) {
            // This is a group chat
            String[] groupInfo = message.getTo().split("##");
            labelFriendName.setText(groupInfo[1]); // Set group name
            labelFriendStatus.setText("Group Chat");
            try {
                imgFriendStatus.setImage(new Image(getClass().getResource("/resouces/group.png").openStream()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
