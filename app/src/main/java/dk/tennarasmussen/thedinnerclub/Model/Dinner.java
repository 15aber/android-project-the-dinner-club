package dk.tennarasmussen.thedinnerclub.Model;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Dinner {
    private long dateTime;
    private String host;
    private String comment;
    public Map<String, Boolean> guests = new HashMap<>();

    public Dinner(long dateTime, String host, String comment) {
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
