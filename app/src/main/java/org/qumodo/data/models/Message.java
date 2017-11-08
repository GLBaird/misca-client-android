package org.qumodo.data.models;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONObject;
import org.qumodo.data.DataLoader;
import org.qumodo.data.DataManager;
import org.qumodo.data.contracts.Messages;
import org.qumodo.network.QMessageType;

import java.text.DateFormat;
import java.util.Date;

public class Message {
    private String id;
    private String fromID;
    private String groupID;
    private Date sent;
    private QMessageType type;
    private String text;
    private Boolean viewed;
    private Context appContext;
    private Boolean sendError = false;

    public Message(Cursor cursor, Context context) {
        this(
                cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry._ID)),
                cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_FROM_ID)),
                cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_GROUP_ID)),
                cursor.getLong(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_TS)),
                cursor.getInt(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_TYPE)),
                cursor.getString(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_DATA)),
                cursor.getInt(cursor.getColumnIndex(Messages.MessagesEntry.COLUMN_NAME_VIEWED)),
                context
        );
    }

    public Message(String id, String from, String group, long ts, int type, String data, int viewed, Context context) {
        this.id = id;
        this.fromID = from;
        this.groupID = group;
        this.sent = new Date(ts);
        this.type = QMessageType.conform(type);
        this.viewed = viewed == 1;
        this.appContext = context.getApplicationContext();

        JSONObject parsed = DataLoader.loadJSONObject(data);
        try {
            if (parsed != null && (this.type == QMessageType.TEXT
                    || this.type == QMessageType.MISCA_QUESTION
                    || this.type == QMessageType.MISCA_TEXT
                    || this.type == QMessageType.MISCA_PHOTO)
                    || this.type == QMessageType.MISCA_FACES) {
                this.text = parsed.getString("text");
            } else if (parsed != null && this.type == QMessageType.PICTURE) {
                this.text = parsed.getString("caption");
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            this.text = this.type == QMessageType.UNKNOWN ? "Unknown message type" : "Error reading message";
        }
    }

    public String getId() {
        return id;
    }

    private User fromUser;
    public User getFrom() {
        if (fromUser == null) {
            DataManager dm = new DataManager(appContext);
            fromUser = dm.getUser(fromID);
        }

        return fromUser;
    }

    private Group messageGroup;

    public Group getGroup() {
        if (messageGroup == null) {
            DataManager dm = new DataManager(appContext);
            messageGroup = dm.getGroup(groupID);
        }

        return messageGroup;
    }

    public long getTS() {
        return sent.getTime();
    }

    public String getSentAsShort() {
        return getSent(DateFormat.SHORT);
    }

    public String getSentAsLong() {
        return getSent(DateFormat.LONG);
    }

    public String getSentAsTime() {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(sent);
    }

    public String getSent(int style) {
        return DateFormat.getDateInstance(style).format(sent);
    }

    public QMessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public Boolean getSendError() {
        return sendError;
    }

    public void setSendError(Boolean error) {
        sendError = error;
    }

    public void setType(QMessageType type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", fromID='" + fromID + '\'' +
                ", groupID='" + groupID + '\'' +
                ", sent=" + sent +
                ", type=" + type +
                ", text='" + text + '\'' +
                ", viewed=" + viewed +
                '}';
    }
}
