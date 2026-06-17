package network;

import java.io.Serializable;

public class Request implements Serializable {
    private String type; // e.g., "LOGIN", "REGISTER", "ADD_COURSE", "GET_COURSES", "SEND_DIRECT_MESSAGE"
    private Object payload; // The actual data being sent (e.g., User object, Message object)

    public Request(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
