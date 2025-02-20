package hr.ekufrin.trainingapplication.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private Long id;
    @SerializedName("message")
    private String message;
    @SerializedName("sentdate")
    private String sentDate;

    public String getSentDate() {
        return sentDate;
    }

    public String getMessage() {
        return message;
    }

    public Long getId() {
        return id;
    }
}
