package org.flinkhub.fhnet;

public class Constants {
    public static final String TAG = "Flinkhub/TGChat";

    public static final String FH_API_BASE = "https://tg.flinkhub.com";
    public static final String LOGO_BASE_URL = "https://logo.clearbit.com/";

    public static final String INVITE_LINK = "http://bit.ly/fhappinvitefriends";

    public static final String GET_USER_DATA_URL = FH_API_BASE + "/profiles/{userId}";
    public static final String SAVE_USER_DATA_URL = FH_API_BASE + "/profiles/{userId}/tg_auth";
    public static final String ADD_EDUCATION_URL = FH_API_BASE + "/profiles/{userId}/education";
    public static final String UPDATE_EDUCATION_URL = FH_API_BASE + "/profiles/{userId}/education/{educationId}";
    public static final String ADD_EXPERIENCE_URL = FH_API_BASE + "/profiles/{userId}/experience";
    public static final String UPDATE_EXPERIENCE_URL = FH_API_BASE + "/profiles/{userId}/experience/{experienceId}";

    public static final String UPDATE_PROFILE_URL = FH_API_BASE + "/profiles/{userId}";
    public static final String SEARCH_COMPANIES_URL = FH_API_BASE + "/companies/suggest";

    public static final String BOT_USERNAME = "FlinkhubBot";
    public static final String BOT_LINK = "https://t.me/" + BOT_USERNAME;

    public static final String FEEDBACK_GROUP_LINK = "https://t.me/joinchat/KwsYSBcevXBQcCsOBbpVow";

    public static String DT_FORMAT_MONTH_YEAR = "MMM yyyy";
    public static String DT_FORMAT_TO_SERVER = "yyyy-MM-dd";
}
