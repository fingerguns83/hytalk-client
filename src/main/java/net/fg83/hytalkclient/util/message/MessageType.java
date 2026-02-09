package net.fg83.hytalkclient.util.message;

public enum MessageType {
    PAIR("pair"),
    ACK("ack"),
    READY("ready"),
    POSITION_DATA("position_data"),
    GROUP_DATA("group_data"),
    PING("ping"),
    PONG("pong");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

    public static MessageType ValueOf(String type) {
        for (MessageType m : MessageType.values()) {
            if (m.getType().equals(type)) {
                return m;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }
}

