package model;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * ClassName : JavaMailAPI.java Description : send mail with java mail API
 *
 * @author MotYim
 * @since 16-02-2017
 */
public class MailModel {

    // -------------- Mail API vars ------------------
    private Properties mailServerProperties;
    private Session getMailSession;
    private MimeMessage generateMailMessage;

    // -------------- local vars ------------------
    // Thay email thật và App Password vào đây
    final private String SENDER_MAIL = "anhvinguyen85@gmail.com";
    final private String PASSWORD = "iojz nzjg snpm gntc"; // Use App Password, not regular password
    final private String SMTP = "smtp.gmail.com";

    // -------------- object vars ------------------
    private String to;
    private String subject;
    private String emailBody;

    public MailModel(String to, String subject, String emailBody) {
        this.to = to;
        this.subject = subject;
        this.emailBody = emailBody;
    }

    public boolean sendMail() {
        try {
            // -------------- setup Mail Server Properties ------------------
            mailServerProperties = new Properties();
            mailServerProperties.put("mail.smtp.host", SMTP);
            mailServerProperties.put("mail.smtp.port", "587");
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail.smtp.starttls.enable", "true");
            mailServerProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // -------------- get Mail Session ------------------
            // Sử dụng Authenticator để xác thực
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_MAIL, PASSWORD);
                }
            };

            getMailSession = Session.getInstance(mailServerProperties, auth);
            getMailSession.setDebug(true); // Bật debug mode để xem lỗi

            generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            generateMailMessage.setSubject("Void Chat | " + subject);
            emailBody += "<br><br>Void Chat Team <br> <a href='https://motyim.github.io/voidChat/'>visit us</a>";
            generateMailMessage.setContent(emailBody, "text/html");

            // -------------- Send mail ------------------
            Transport.send(generateMailMessage);
            System.out.println("Email sent successfully to " + to);

        } catch (AddressException ex) {
            System.err.println("Address error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (MessagingException ex) {
            System.err.println("Messaging error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.err.println("General error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
