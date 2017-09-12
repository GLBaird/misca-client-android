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
    String id;
    String fromID;
    String groupID;
    Date sent;
    QMessageType type;
    String text;
    Boolean viewed;
    Context appContext;

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
            if (parsed != null && this.type == QMessageType.TEXT) {
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
        if (fromID == null) {
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
