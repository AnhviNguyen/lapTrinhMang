package view;

import javafx.scene.paint.Color;

/**
 * Lớp tiện ích để tạo nội dung email HTML
 */
public class CreateHtmlEmail {
    
    /**
     * Tạo email HTML với định dạng đầy đủ
     * 
     * @param subject      Chủ đề email
     * @param senderEmail  Email người gửi
     * @param message      Nội dung tin nhắn
     * @param fontFamily   Tên font
     * @param fontSize     Kích thước font
     * @param isBold       Có in đậm hay không
     * @param isItalic     Có in nghiêng hay không
     * @param isUnderline  Có gạch chân hay không
     * @param colorHex     Mã màu dạng hex
     * @return Nội dung HTML của email
     */
    public static String createHtmlEmail(String subject, String senderEmail, String message,
            String fontFamily, String fontSize, boolean isBold, boolean isItalic, 
            boolean isUnderline, String colorHex) {
        
        // Tạo style CSS
        StringBuilder cssStyle = new StringBuilder();
        cssStyle.append("font-family: '").append(fontFamily).append("', sans-serif; ");
        cssStyle.append("font-size: ").append(fontSize).append("px; ");
        cssStyle.append("color: ").append(colorHex).append("; ");
        
        // Tạo HTML
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
        htmlBuilder.append("<style>body { font-family: Arial, sans-serif; line-height: 1.6; }</style>");
        htmlBuilder.append("</head><body>");
        
        // Thêm tiêu đề
        htmlBuilder.append("<h1 style=\"font-family: Arial, sans-serif; color: #1877f2;\">")
                   .append(subject)
                   .append("</h1>");
        
        // Thêm dòng chữ "From: email@example.com"
        htmlBuilder.append("<div style=\"margin-bottom: 20px; color: #777; font-size: 14px;\">");
        htmlBuilder.append("<strong>From: </strong>").append(senderEmail).append("</div>");
        
        // Phân tách các đoạn văn bản
        String[] paragraphs = message.split("\n\n");
        
        // Xử lý từng đoạn văn bản
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) continue;
            
            // Mở thẻ đoạn với style
            htmlBuilder.append("<p style=\"").append(cssStyle).append("\">");
            
            // Xử lý các dòng trong đoạn
            String[] lines = paragraph.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                // Áp dụng định dạng
                if (isBold) line = "<strong>" + line + "</strong>";
                if (isItalic) line = "<em>" + line + "</em>";
                if (isUnderline) line = "<u>" + line + "</u>";
                
                htmlBuilder.append(line);
                
                // Thêm dấu ngắt dòng nếu không phải dòng cuối
                if (i < lines.length - 1) {
                    htmlBuilder.append("<br>");
                }
            }
            
            htmlBuilder.append("</p>");
        }
        
        // Thêm chân email
        htmlBuilder.append("<div style=\"margin-top: 30px; border-top: 1px solid #eee; padding-top: 15px; ");
        htmlBuilder.append("color: #777; font-size: 12px;\">");
        htmlBuilder.append("Sent from VoidChat Application</div>");
        
        htmlBuilder.append("</body></html>");
        
        return htmlBuilder.toString();
    }
    
    /**
     * Chuyển đổi màu sang mã hex
     * 
     * @param color Đối tượng Color
     * @return Chuỗi mã màu hex
     */
    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
} 