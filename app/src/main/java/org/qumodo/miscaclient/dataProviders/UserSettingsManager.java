package org.qumodo.miscaclient.dataProviders;

public class UserSettingsManager {

    private static String userID = "QSDU_1122334455";

    public static final String USER_ID_A = "QSDU_1122334455";
    public static final String USER_ID_B = "QSDU_3944238293";

    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String userID) {
        UserSettingsManager.userID = userID;
    }

    public static String getMiscaID() {
        return "QSDU_0000000000";
    }

}
