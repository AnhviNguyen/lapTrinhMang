package model;

import java.io.File;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * ClassName : VoiceMessage.java
 * Description : class to represent voice messages in chat
 */
public class VoiceMessage extends Message {

    private byte[] audioData;
    private int duration; // duration in seconds
    private String fileExtension; // "wav", "mp3", etc.

    public VoiceMessage() {
        super();
    }

    public VoiceMessage(int fontsSize, String from, String to, XMLGregorianCalendar date,
            String fontColor, String fontFamily, String fontStyle,
            String body, String fontWeight, Boolean underline,
            byte[] audioData, int duration, String fileExtension) {
        super(fontsSize, from, to, date, fontColor, fontFamily, fontStyle, body, fontWeight, underline);
        this.audioData = audioData;
        this.duration = duration;
        this.fileExtension = fileExtension;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
}