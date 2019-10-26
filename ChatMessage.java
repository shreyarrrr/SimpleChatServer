/**
 * Chat Message
 *
 * @author Shreya Roy
 * @version 11/27/18
 */
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    String message;
    String recipient;
    int type; //0 for general, 1 for logout, 2 for recipient

    public ChatMessage(String message, int type) {
        this.message = message;
        if (type >= 0 || type <= 1)
            this.type = type;
    }
    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.recipient = recipient;
        if (type >= 0 || type <= 2)
            this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }
}
