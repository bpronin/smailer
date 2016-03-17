package com.bopr.android.smailer;

import java.util.Date;

/**
 * Class ActivityLogItem.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ActivityLogItem {

    private Long id;
    private Date time;
    private String message;
    private int level;
    private String details;

    public ActivityLogItem(int level) {
        this.level = level;
        this.time = new Date();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ActivityLogItem{" +
                "id=" + id +
                ", time=" + time +
                ", message='" + message + '\'' +
                ", level=" + level +
                ", details='" + details + '\'' +
                '}';
    }
}
