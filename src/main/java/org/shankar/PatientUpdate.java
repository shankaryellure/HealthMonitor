package org.shankar;

import java.io.Serializable;

public class PatientUpdate implements Serializable {
    private String clientId;
    private String category;
    private String message;
    private int priorityLevel;

    public PatientUpdate(String clientId, String category, String message, int priorityLevel) {
        this.clientId = clientId;
        this.category = category;
        this.message = message;
        this.priorityLevel = priorityLevel;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getPriorityLevel() {
        return priorityLevel;
    }
}

