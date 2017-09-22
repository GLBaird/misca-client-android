package org.qumodo.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Formatter;
import java.util.UUID;

public class QMessage {

    public String id;
    public String[] to;
    public String from;
    public QMessageType type;
    public JSONObject data;
    public long ts;

    public static final String KEY_DEVICE_ID = "deviceID";
    public static final String KEY_PUBLIC_KEY_HASH = "certificate";
    public static final String KEY_PASS_PHRASE = "passPhrase";
    public static final String KEY_GROUP_ID = "groupID";
    public static final String KEY_USER_AUTHENTICATION = "authenticated";
    public static final String KEY_MISCA_ID = "miscaID";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_CAPTION = "caption";
    public static final String KEY_MISCA_QUESTION = "miscaQuestion";
    public static final String KEY_MISCA_QUESTION_LIST = "miscaQuestionList";
    public static final String KEY_MISCA_RESPONSE = "miscaResponse";
    public static final String KEY_MISCA_CAPTION = "miscaCaption";

    public static QMessage make(String messageJSON) throws JSONException {
        JSONObject parsed = new JSONObject(messageJSON);
        JSONObject data = parsed.getJSONObject("data");
        JSONArray toArray = parsed.getJSONArray("to");
        String[] to = new String[toArray.length()];
        for (int i = 0; i < to.length; i++) {
            to[i] = toArray.optString(i);
        }
        return new QMessage(parsed.getString("id"), to, parsed.getString("from"), parsed.getInt("type"), data, parsed.getLong("ts"));
    }

    private static String getUUID() {
        return UUID.randomUUID().toString();
    }

    private static long getTimeStamp() {
        return new Date().getTime();
    }

    public QMessage() {
        this.id = getUUID();
        this.ts = getTimeStamp();
        this.type = QMessageType.UNKNOWN;
        to = new String[0];
        from = "";
        try {
            data = new JSONObject("{}");
        } catch (JSONException e) { /* ignore */ }
    }

    public QMessage(String[] to, String from, QMessageType type, JSONObject data) {
        this(getUUID(), to, from, type, data, getTimeStamp());
    }

    public QMessage(String to, String from, QMessageType type, JSONObject data) {
        this(getUUID(), new String[]{to}, from, type, data, getTimeStamp());
    }

    public QMessage(String id, String[] to, String from, int type, JSONObject data, long ts) {
        this(id, to, from, QMessageType.conform(type), data, ts);
    }

    public QMessage(String id, String[] to, String from, QMessageType type, JSONObject data, long ts) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.type = type;
        this.data = data;
        this.ts = ts;
    }

    public String serialize() throws JSONException {
        JSONObject export = new JSONObject();
        export.put("id", id);
        JSONArray toValues = new JSONArray(to);
        export.put("to", toValues);
        export.put("from", from);
        export.put("data", data);
        export.put("ts", ts);
        export.put("type", type.value);
        export.put("typeText", type.getText());

        return export.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<to.length; i++) {
            sb.append(to[i]);
            if (to.length > 1 && i<(to.length-1)) {
                sb.append(',');
            }
        }
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format("id: %s, type: %d, from: %s, to: %s, ts: %d", id, type.value, from, sb.toString(), ts);
        return fmt.toString();
    }

    public void addData(String key, Object value) throws JSONException {
        data.put(key, value);
    }

    public void addData(String key, int value) throws JSONException {
        data.put(key, value);
    }

    public void addData(String key, boolean value) throws JSONException {
        data.put(key, value);
    }

    public void addData(String key, long value) throws JSONException {
        data.put(key, value);
    }

    public void addData(String key, double value) throws JSONException {
        data.put(key, value);
    }

    public Object get(String name) throws JSONException {
        return data.get(name);
    }

    public int getInt(String name) throws JSONException {
        return data.getInt(name);
    }

    public long getLong(String name) throws JSONException {
        return data.getLong(name);
    }

    public boolean getBool(String name) throws JSONException {
        return data.getBoolean(name);
    }

    public double getDouble(String name) throws JSONException {
        return data.getDouble(name);
    }


}
