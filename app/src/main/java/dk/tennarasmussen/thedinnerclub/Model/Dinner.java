package dk.tennarasmussen.thedinnerclub.Model;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Dinner {
    private long dateTime;
    private User host;
    private String comment;
    private String imageURL;
    public Map<String, Boolean> guests = new HashMap<>();

    public Dinner(long dateTime, User host, String comment, String imageURL) {
        this.dateTime = dateTime;
        this.host = host;
        this.comment = comment;
        this.imageURL = imageURL;
    }

    public Dinner(long dateTime, User host, String comment) {
        this.dateTime = dateTime;
        this.host = host;
        this.comment = comment;
    }

    public Dinner() {
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("dateTime", dateTime);
        result.put("host", host);
        result.put("imageURL", imageURL);
        result.put("comment", comment);
        result.put("guests", guests);

        return result;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
