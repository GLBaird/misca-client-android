package org.qumodo.network;

public enum QMessageType {
    TEXT(0),
    PICTURE(1),
    MISCA_QUESTION(2),
    MISCA_RESPONSE(3),
    JOINED_GROUP(4),
    LEFT_GROUP(5),
    TYPING_STARTED(6),
    TYPING_ENDED(7),
    ERROR(8),
    COMMAND(9),
    NEW_CONNECTION(10),
    SIGN_UP(11),
    ADD_DEVICE(12),
    UNKNOWN(100);

    public int value;

    public String getText() {
        switch (value) {
            case 0:
                return "text";
            case 1:
                return "picture";
            case 2:
                return "miscaQuestion";
            case 3:
                return "miscaResponse";
            case 4:
                return "joinedGroup";
            case 5:
                return "leftGroup";
            case 6:
                return "typingStarted";
            case 7:
                return "typingEnded";
            case 8:
                return "error";
            case 9:
                return "command";
            case 10:
                return "newConnection";
            case 11:
                return "signUp";
            case 12:
                return "addDevice";
            default:
                return "unknown";
        }
    }

    static QMessageType conform(int val) {
        for (QMessageType type : QMessageType.values()) {
            if (type.value == val) {
                return type;
            }
        }
        return QMessageType.UNKNOWN;
    }

    QMessageType(int val) {
        value = val;
    }
}
