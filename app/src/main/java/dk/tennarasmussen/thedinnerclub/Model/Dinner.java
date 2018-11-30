package dk.tennarasmussen.thedinnerclub.Model;

import java.util.Date;

public class Dinner {
    private String dateTime;
    private String host;
    private String comment;

    public Dinner(String dateTime, String host, String comment) {
        this.dateTime = dateTime;
        this.host = host;
        this.comment = comment;
    }

    public Dinner() {
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
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
