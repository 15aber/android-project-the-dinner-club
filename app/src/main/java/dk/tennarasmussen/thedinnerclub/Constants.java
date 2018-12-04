package dk.tennarasmussen.thedinnerclub;

public interface Constants {
    String FB_DB_USER = "users";
    String FB_DB_DINNER_CLUB = "dinnerClub";
    String FB_DB_DINNER_CLUBS = "dinnerClubs";
    String FB_DB_CLUB_INVITATION = "clubInvitation";
    String FB_DB_CLUB_INVITATIONS = "clubInvitations";
    String FB_DB_DINNERS = "dinners";

    String LOGIN_EMAIL = "dk.tennarasmussen.thedinnerclub.LOGIN_EMAIL";
    String LOGIN_PASS = "dk.tennarasmussen.thedinnerclub.LOGIN_PASS";
    String REGISTER_NAME = "dk.tennarasmussen.thedinnerclub.REGISTER_NAME";
    String REGISTER_PHONE = "dk.tennarasmussen.thedinnerclub.REGISTER_PHONE";
    String REGISTER_STREET = "dk.tennarasmussen.thedinnerclub.REGISTER_STREET";
    String REGISTER_ZIP = "dk.tennarasmussen.thedinnerclub.REGISTER_ZIP";
    String REGISTER_CITY = "dk.tennarasmussen.thedinnerclub.REGISTER_CITY";
    String DINNER_DATETIME = "dk.tennarasmussen.thedinnerclub.DINNER_DATETIME";
    String DINNER_MESSAGE = "dk.tennarasmussen.thedinnerclub.DINNER_MESSAGE";
    String DINNER_IMAGE_URL = "dk.tennarasmussen.thedinnerclub.DINNER_IMAGE_URL";
    String DINNER_LIST_POSITION = "dk.tennarasmussen.thedinnerclub.DINNER_LIST_POSITION";

    String BROADCAST_USER_UPDATED = "dk.tennarasmussen.thedinnerclub.BROADCAST_USER_UPDATED";
    String BROADCAST_DINNER_CLUB_UPDATED = "dk.tennarasmussen.thedinnerclub.BROADCAST_DINNER_CLUB_UPDATED";
    String BROADCAST_LOADED_DC_INVITATION = "dk.tennarasmussen.thedinnerclub.BROADCAST_LOADED_DC_INVITATION";
    String BROADCAST_DINNERS_UPDATED = "dk.tennarasmussen.thedinnerclub.BROADCAST_DINNERS_UPDATED";

    int REGISTER_REQUEST = 1;
    int LOGIN_REQUEST = 2;
    int NEW_DINNER_REQUEST = 3;

    int NOTIFY_ID = 142;
}
