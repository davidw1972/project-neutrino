package org.flinkhub.fhnet;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;

import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.Experience;
import org.flinkhub.fhnet.models.UserDataObj;
import org.flinkhub.messenger2.AndroidUtilities;
import org.flinkhub.messenger2.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.io.IOException;
import java.util.Vector;

public class UserData {
    private static SparseArray users = new SparseArray<UserDataObj>();
    private static SparseBooleanArray loadingUsers = new SparseBooleanArray();

    public static UserDataObj getUser(int user_id, int currentAccount) {
        UserDataObj user = (UserDataObj) users.get(user_id);
        if (user != null) {
            return user;
        }

        if (loadingUsers.get(user_id, false)) {
            return null;
        }

        loadingUsers.put(user_id, true);

        new Thread(() -> {
            String url = Constants.GET_USER_DATA_URL.replace("{userId}", Integer.toString(user_id));
            try {
                String response = WebRequest.get(url);
                UserDataObj u = parseProfileJSON(response);

                // Notify user loaded
                AndroidUtilities.runOnUIThread(() -> {
                    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.fhUserInfoDidLoad, user_id, u, null);
                });
            } catch (JSONException ex) {
                // TODO Handle JSON Parsing failure
                Log.e(Constants.TAG, "JSON Parsing failed");
                Log.e(Constants.TAG, ex.getMessage());
            } catch (IOException ex) {
                // TODO Handle request failure
                Log.e(Constants.TAG, "Request failed");
                Log.e(Constants.TAG, ex.getMessage());
            }

            loadingUsers.put(user_id, false);
        }).start();

        return null;
    }

    private static String getStringFromJSON(JSONObject j, String key, String defaultValue) throws JSONException {
        if (!j.has(key)) {
            return defaultValue;
        }

        if (j.isNull(key)) {
            return defaultValue;
        }

        String val = j.getString(key);
        if (val == null || val.length() == 0) {
            return defaultValue;
        }

        return val;
    }

    private static String getStringFromJSON(JSONObject j, String key) throws JSONException {
        return getStringFromJSON(j, key, "");
    }

    private static UserDataObj parseProfileJSON(String response) throws JSONException {
        UserDataObj u = new UserDataObj();

        JSONObject j = new JSONObject(response);
        if (!j.has("success") || !j.getBoolean("success")) {
            u.setCorrect(false);
            String errorMessage = j.has("error") ? j.getString("error") : "";
            u.setErrorMessage(errorMessage);

            return u;
        }

        j = j.getJSONObject("profile");
        u.setLocationStr(getStringFromJSON(j, "locationStr"));
        u.setUser_id(j.optInt("telegramUserId", 0));

        Vector<Experience> education = new Vector<>();
        Vector<Experience> experience = new Vector<>();

        if (j.has("education")) {
            JSONArray a1 = j.getJSONArray("education");
            for (int i = 0; i < a1.length(); i++) {
                JSONObject e = a1.getJSONObject(i);
                Experience ex = new Experience();
                ex.setId(getStringFromJSON(e, "_id"));
                ex.setExperienceType(Experience.EDUCATION);
                ex.setDegreeStr(getStringFromJSON(e, "degree"));
                ex.setDescription(getStringFromJSON(e, "description"));

                if (e.has("company")) {
                    JSONObject comp = e.getJSONObject("company");
                    Company c = new Company();
                    c.setId(getStringFromJSON(comp, "_id"));
                    c.setName(getStringFromJSON(comp, "name"));
                    c.setDomainName(getStringFromJSON(comp, "domainName"));
                    ex.setCompany(c);
                }

                ex.setCurrent(e.optBoolean("isCurrent", false));

                String startDateStr = getStringFromJSON(e, "startDate");
                String endDateStr = getStringFromJSON(e, "endDate");

                if (startDateStr.length() > 0) {
                    ex.setStartDate(ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(e.getString("startDate")));
                } else {
                    ex.setStartDate(null);
                }

                if (endDateStr.equals(startDateStr)) {
                    ex.setEndDate(null);
                } else if (endDateStr.length() > 0 && !ex.isCurrent()) {
                    ex.setEndDate(ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(e.getString("endDate")));
                } else {
                    ex.setEndDate(null);
                }

                education.add(ex);
            }
        }

        if (j.has("experience")) {
            JSONArray a2 = j.getJSONArray("experience");
            for (int i = 0; i < a2.length(); i++) {
                JSONObject e = a2.getJSONObject(i);
                Experience ex = new Experience();

                ex.setId(getStringFromJSON(e, "_id"));
                ex.setExperienceType(Experience.WORK_EXP);
                ex.setRoleStr(getStringFromJSON(e, "jobTitle"));
                ex.setDescription(getStringFromJSON(e, "description"));

                if (e.has("company")) {
                    JSONObject comp = e.getJSONObject("company");
                    Company c = new Company();
                    c.setId(getStringFromJSON(comp, "_id"));
                    c.setName(getStringFromJSON(comp, "name"));
                    c.setDomainName(getStringFromJSON(comp, "domainName"));
                    ex.setCompany(c);
                }

                ex.setCurrent(e.getBoolean("isCurrent"));

                String startDateStr = getStringFromJSON(e, "startDate");
                String endDateStr = getStringFromJSON(e, "endDate");

                if (startDateStr.length() > 0) {
                    ex.setStartDate(ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(e.getString("startDate")));
                } else {
                    ex.setStartDate(null);
                }

                if (endDateStr.equals(startDateStr)) {
                    ex.setEndDate(null);
                } else if (endDateStr.length() > 0 && !ex.isCurrent()) {
                    ex.setEndDate(ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(e.getString("endDate")));
                } else {
                    ex.setEndDate(null);
                }

                experience.add(ex);
            }
        }

        if (j.has("companyId")) {
            u.setCompanyId(j.getString("companyId"));
        }

        if (j.has("company")) {
            JSONObject comp = j.getJSONObject("company");
            Company company = new Company();
            company.setId(comp.optString("_id", ""));
            company.setName(comp.optString("name", ""));
            company.setDomainName(comp.optString("domainName", ""));
            u.setCompany(company);

            if (u.getCompanyId().length() == 0) {
                u.setCompanyId(company.getId());
            }
        }

        u.setEducation(education);
        u.setExperience(experience);
        u.setCompanyId(getStringFromJSON(j, "companyId"));
        u.setBotStage(getStringFromJSON(j, "botStage"));
        u.setBatch(getStringFromJSON(j, "batch"));
        u.setCorrect(true);

        int user_id = u.getUser_id();
        users.put(user_id, u);
        return u;
    }

    public static boolean isLoading(int user_id) {
        return loadingUsers.get(user_id, false);
    }

    public static void saveUserInfo(TLRPC.User user) {
        new Thread(() -> {
            String url = Constants.SAVE_USER_DATA_URL.replace("{userId}", Integer.toString(user.id));
            try {
                JSONObject u = new JSONObject();
                u.put("id", user.id);
                u.put("first_name", user.first_name);
                u.put("last_name", user.last_name);
                u.put("username", user.username);
                u.put("access_hash", Long.toString(user.access_hash));
                u.put("phone", user.phone);
                u.put("lang_code", user.lang_code);
                u.put("verified", user.verified);
                u.put("scam", user.scam);

                JSONObject j = new JSONObject();
                j.put("user", u);
                String response = WebRequest.post(url, j.toString());
            } catch (JSONException ex) {
                // TODO Handle request failure
                Log.e(Constants.TAG, "Request failed due to bad json");
                Log.e(Constants.TAG, ex.getMessage());
            } catch (IOException ex) {
                // TODO Handle request failure
                Log.e(Constants.TAG, "Request failed");
                Log.e(Constants.TAG, ex.getMessage());
            }
        }).start();
    }

    public static UserDataObj addEducation(int user_id, String postData) {
        String url = Constants.ADD_EDUCATION_URL.replace("{userId}", Integer.toString(user_id));
        UserDataObj result;

        try {
            String response = WebRequest.post(url, postData);
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj updateEducation(int user_id, String experience_id, String postData) {
        String url = Constants.UPDATE_EDUCATION_URL.replace("{userId}", Integer.toString(user_id));
        url = url.replace("{educationId}", experience_id);
        UserDataObj result;

        try {
            String response = WebRequest.put(url, postData);
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj deleteEducation(int user_id, String education_id) {
        String url = Constants.UPDATE_EDUCATION_URL.replace("{userId}", Integer.toString(user_id));
        url = url.replace("{educationId}", education_id);
        UserDataObj result;

        try {
            String response = WebRequest.delete(url, "");
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj addExperience(int user_id, String postData) {
        String url = Constants.ADD_EXPERIENCE_URL.replace("{userId}", Integer.toString(user_id));
        UserDataObj result;

        try {
            String response = WebRequest.post(url, postData);
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj updateExperience(int user_id, String experience_id, String postData) {
        String url = Constants.UPDATE_EXPERIENCE_URL.replace("{userId}", Integer.toString(user_id));
        url = url.replace("{experienceId}", experience_id);
        UserDataObj result;

        try {
            String response = WebRequest.put(url, postData);
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj deleteExperience(int user_id, String experience_id) {
        String url = Constants.UPDATE_EXPERIENCE_URL.replace("{userId}", Integer.toString(user_id));
        url = url.replace("{experienceId}", experience_id);

        UserDataObj result;

        try {
            String response = WebRequest.delete(url, "");
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static UserDataObj updateProfileData(int user_id, String postData) {
        String url = Constants.UPDATE_PROFILE_URL.replace("{userId}", Integer.toString(user_id));
        UserDataObj result;

        try {
            String response = WebRequest.patch(url, postData);
            result = parseProfileJSON(response);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new UserDataObj();
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }
}
