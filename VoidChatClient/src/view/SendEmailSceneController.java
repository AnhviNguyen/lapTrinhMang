/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.User;

/**
 * Controller for the Send Email Scene
 * 
 * @author VoidChat Team
 */
public class SendEmailSceneController implements Initializable {

    @FXML
    private TextField txtFieldTo;

    @FXML
    private TextField txtFieldSubject;

    @FXML
    private TextArea txtAreaMessage;

    @FXML
    private ToggleButton boldToggleBtn;

    @FXML
    private ToggleButton italicToggleBtn;

    @FXML
    private ToggleButton underlineToggleBtn;

    @FXML
    private ComboBox<String> fontComboBox;

    @FXML
    private ComboBox<String> fontSizeComboBox;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Button closeButton;

    private String senderEmail;
    private String recipientEmail;

    // Định dạng mặc định
    private String defaultFont;
    private String defaultSize;
    private boolean defaultBold;
    private boolean defaultItalic;
    private boolean defaultUnderline;
    private String defaultColor;

    private ClientView clientView;

    /**
     * Default constructor used by FXML loader
     */
    public SendEmailSceneController() {
        clientView = ClientView.getInstance();
        this.senderEmail = null;
        this.recipientEmail = null;
        this.defaultFont = "Arial";
        this.defaultSize = "12";
        this.defaultBold = false;
        this.defaultItalic = false;
        this.defaultUnderline = false;
        this.defaultColor = "#000000";
    }

    /**
     * Constructor with formatting parameters
     * 
     * @param senderEmail    Email của người gửi
     * @param recipientEmail Email của người nhận
     * @param font           Font chữ
     * @param fontSize       Kích thước chữ
     * @param isBold         In đậm
     * @param isItalic       In nghiêng
     * @param isUnderline    Gạch chân
     * @param color          Màu chữ
     */
    public SendEmailSceneController(String senderEmail, String recipientEmail,
            String font, String fontSize, boolean isBold, boolean isItalic,
            boolean isUnderline, String color) {

        clientView = ClientView.getInstance();
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
        this.defaultFont = font != null ? font : "Arial";
        this.defaultSize = fontSize != null ? fontSize : "12";
        this.defaultBold = isBold;
        this.defaultItalic = isItalic;
        this.defaultUnderline = isUnderline;
        this.defaultColor = color != null ? color : "#000000";
    }

    /**
     * Initialize the controller
     * 
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Đặt email người nhận
        if (recipientEmail != null) {
            txtFieldTo.setText(recipientEmail);
        }

        // Thiết lập các tùy chọn font
        fontComboBox.getItems().addAll("Arial", "Times New Roman", "Calibri", "Courier New", "Verdana", "Tahoma",
                "Helvetica");
        fontComboBox.setValue(defaultFont);

        // Thiết lập các tùy chọn kích thước
        fontSizeComboBox.getItems().addAll("10", "11", "12", "14", "16", "18", "20", "24", "28", "32");
        fontSizeComboBox.setValue(defaultSize);

        // Thiết lập các nút định dạng
        boldToggleBtn.setSelected(defaultBold);
        italicToggleBtn.setSelected(defaultItalic);
        underlineToggleBtn.setSelected(defaultUnderline);

        // Thiết lập màu chữ
        try {
            colorPicker.setValue(Color.web(defaultColor));
        } catch (Exception e) {
            colorPicker.setValue(Color.BLACK);
        }

        // Cập nhật định dạng khi nội dung thay đổi
        setupTextFormatting();
    }

    /**
     * Thiết lập sự kiện cho các điều khiển định dạng văn bản
     */
    private void setupTextFormatting() {
        // Cập nhật định dạng khi thay đổi font
        fontComboBox.setOnAction(e -> updateTextFormat());

        // Cập nhật định dạng khi thay đổi cỡ chữ
        fontSizeComboBox.setOnAction(e -> updateTextFormat());

        // Cập nhật định dạng khi thay đổi kiểu chữ
        boldToggleBtn.setOnAction(e -> updateTextFormat());
        italicToggleBtn.setOnAction(e -> updateTextFormat());
        underlineToggleBtn.setOnAction(e -> updateTextFormat());

        // Cập nhật định dạng khi thay đổi màu
        colorPicker.setOnAction(e -> updateTextFormat());

        // Bắt phím tắt
        txtAreaMessage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                if (event.getCode() == KeyCode.B) {
                    boldToggleBtn.setSelected(!boldToggleBtn.isSelected());
                    updateTextFormat();
                    event.consume();
                } else if (event.getCode() == KeyCode.I) {
                    italicToggleBtn.setSelected(!italicToggleBtn.isSelected());
                    updateTextFormat();
                    event.consume();
                } else if (event.getCode() == KeyCode.U) {
                    underlineToggleBtn.setSelected(!underlineToggleBtn.isSelected());
                    updateTextFormat();
                    event.consume();
                }
            }
        });

        // Áp dụng định dạng ban đầu
        updateTextFormat();
    }

    /**
     * Cập nhật định dạng của vùng văn bản
     */
    private void updateTextFormat() {
        // Lưu vị trí con trỏ
        int caretPosition = txtAreaMessage.getCaretPosition();

        // Lấy các giá trị định dạng từ điều khiển
        String fontFamily = fontComboBox.getValue();
        String fontSize = fontSizeComboBox.getValue();
        String fontWeight = boldToggleBtn.isSelected() ? "bold" : "normal";
        String fontStyle = italicToggleBtn.isSelected() ? "italic" : "normal";
        String textDecoration = underlineToggleBtn.isSelected() ? "underline" : "none";

        // Tạo mã màu hex
        Color color = colorPicker.getValue();
        String colorHex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));

        // Tạo chuỗi CSS
        StringBuilder styleBuilder = new StringBuilder();
        styleBuilder.append("-fx-font-family: \"").append(fontFamily).append("\"; ");
        styleBuilder.append("-fx-font-size: ").append(fontSize).append("px; ");
        styleBuilder.append("-fx-font-weight: ").append(fontWeight).append("; ");
        styleBuilder.append("-fx-font-style: ").append(fontStyle).append("; ");
        styleBuilder.append("-fx-text-fill: ").append(colorHex).append("; ");

        if (!"none".equals(textDecoration)) {
            styleBuilder.append("-fx-underline: true; ");
        }

        // Áp dụng style
        txtAreaMessage.setStyle(styleBuilder.toString());

        // Khôi phục vị trí con trỏ
        txtAreaMessage.positionCaret(caretPosition);
    }

    /**
     * Xử lý sự kiện khi nhấn nút Gửi
     * 
     * @param event
     */
    @FXML
    private void sendBtnAction(ActionEvent event) {
        try {
            // Kiểm tra các trường bắt buộc
            String to = txtFieldTo.getText().trim();
            String subject = txtFieldSubject.getText().trim();
            String message = txtAreaMessage.getText().trim();

            if (to.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Missing Recipient",
                        "Please enter a recipient email address.");
                txtFieldTo.requestFocus();
                return;
            }

            if (subject.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Missing Subject",
                        "Please enter an email subject.");
                txtFieldSubject.requestFocus();
                return;
            }

            if (message.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Empty Message",
                        "Please enter a message.");
                txtAreaMessage.requestFocus();
                return;
            }

            // Lấy thông tin người gửi nếu chưa được thiết lập
            if (senderEmail == null || senderEmail.isEmpty()) {
                User currentUser = clientView.getUserInformation();
                senderEmail = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "";
            }

            // Lấy các thông tin định dạng
            String fontFamily = fontComboBox.getValue();
            String fontSize = fontSizeComboBox.getValue();
            boolean isBold = boldToggleBtn.isSelected();
            boolean isItalic = italicToggleBtn.isSelected();
            boolean isUnderline = underlineToggleBtn.isSelected();
            String colorHex = CreateHtmlEmail.toRGBCode(colorPicker.getValue());

            // Tạo HTML email với lớp tiện ích
            String htmlContent = CreateHtmlEmail.createHtmlEmail(
                    subject, senderEmail, message,
                    fontFamily, fontSize, isBold, isItalic, isUnderline, colorHex);

            // Gửi email với đầy đủ tham số
            boolean success = clientView.sendMail(to, subject, htmlContent, senderEmail);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Email Sent",
                        "Your email has been sent successfully.");
                closeWindow(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to Send Email",
                        "There was a problem sending your email. Please try again later.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error Sending Email",
                    "An error occurred: " + ex.getMessage());
        }
    }

    /**
     * Xử lý sự kiện khi nhấn nút Hủy
     * 
     * @param event
     */
    @FXML
    private void cancelBtnAction(ActionEvent event) {
        closeWindow(event);
    }

    /**
     * Đóng cửa sổ gửi email
     * 
     * @param event
     */
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Hiển thị hộp thoại thông báo
     * 
     * @param type    Loại cảnh báo
     * @param title   Tiêu đề
     * @param header  Tiêu đề phụ
     * @param content Nội dung
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
