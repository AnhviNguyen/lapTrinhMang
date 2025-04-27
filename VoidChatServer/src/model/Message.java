package model;

import java.io.Serializable;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * ClassName : Message.java
 * Description : class to represent message in chat
 * 
 * @author MotYim
 * @since 11-02-2017
 */
public class Message implements Serializable {

    protected int fontsSize;
    protected String from;
    protected String to;
    protected XMLGregorianCalendar date;
    protected String fontColor;
    protected String fontFamily;
    protected String fontStyle;
    protected String body;
    protected String fontWeight;
    protected Boolean underline;
    protected String type; // Added type field for voice call and other message types
    protected String content; // Added content field for additional data like call information

    public Message() {

    }

    public Message(int fontsSize, String from, String to, XMLGregorianCalendar date, String fontColor,
            String fontFamily, String fontStyle, String body, String fontWeight, Boolean underline) {
        this.fontsSize = fontsSize;
        this.from = from;
        this.to = to;
        this.date = date;
        this.fontColor = fontColor;
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.body = body;
        this.fontWeight = fontWeight;
        this.underline = underline;
    }

    public Message(int fontsSize, String from, String to, XMLGregorianCalendar date, String fontColor,
            String fontFamily, String fontStyle, String body, String fontWeight, Boolean underline, String type) {
        this.fontsSize = fontsSize;
        this.from = from;
        this.to = to;
        this.date = date;
        this.fontColor = fontColor;
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.body = body;
        this.fontWeight = fontWeight;
        this.underline = underline;
        this.type = type;
    }

    public Message(int fontsSize, String from, String to, XMLGregorianCalendar date, String fontColor,
            String fontFamily, String fontStyle, String body, String fontWeight, Boolean underline, String type,
            String content) {
        this.fontsSize = fontsSize;
        this.from = from;
        this.to = to;
        this.date = date;
        this.fontColor = fontColor;
        this.fontFamily = fontFamily;
        this.fontStyle = fontStyle;
        this.body = body;
        this.fontWeight = fontWeight;
        this.underline = underline;
        this.type = type;
        this.content = content;
    }

    public int getFontsSize() {
        return fontsSize;
    }

    public void setFontsSize(int fontsSize) {
        this.fontsSize = fontsSize;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public XMLGregorianCalendar getDate() {
        return date;
    }

    public void setDate(XMLGregorianCalendar date) {
        this.date = date;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }

    public Boolean getUnderline() {
        return underline;
    }

    public void setUnderline(Boolean underline) {
        this.underline = underline;
    }

    /**
     * Get the message type (e.g., "voice-call-request", "voice-call-accepted",
     * etc.)
     * 
     * @return The message type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the message type
     * 
     * @param type The message type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the content (used for voice call and other special messages)
     * 
     * @return The content string
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the content
     * 
     * @param content The content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
}
