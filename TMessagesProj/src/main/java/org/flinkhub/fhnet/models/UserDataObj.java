package org.flinkhub.fhnet.models;

import java.io.Serializable;
import java.util.Vector;

public class UserDataObj implements Serializable {
    private int user_id = 0;
    private String _id = "";
    private String companyId = "";
    private String locationStr = "";
    private Company company = null;
    private Vector<Experience> experience = new Vector<>();
    private Vector<Experience> education = new Vector<>();
    private String botStage = "";
    private String batch = "";
    private boolean isVerified = false;
    private boolean isCorrect = false;
    private String errorMessage = "";

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLocationStr() {
        return locationStr;
    }

    public void setLocationStr(String locationStr) {
        this.locationStr = locationStr;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Vector<Experience> getExperience() {
        return experience;
    }

    public void setExperience(Vector<Experience> experience) {
        this.experience = experience;
    }

    public Vector<Experience> getEducation() {
        return education;
    }

    public void setEducation(Vector<Experience> education) {
        this.education = education;
    }

    public void addEducation(Experience education) {
        this.education.add(education);
    }

    public void addExperience(Experience experience) {
        this.experience.add(experience);
    }

    public boolean hasEducation() {
        return this.education.size() > 0;
    }

    public boolean hasExperience() {
        return this.experience.size() > 0;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getBotStage() {
        return botStage;
    }

    public void setBotStage(String botStage) {
        this.botStage = botStage;
    }

    public boolean hasUniversity() {
        return this.companyId.length() > 0 && this.company != null;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public boolean hasBatch() {
        return this.batch.length() > 0;
    }

    @Override
    public String toString() {
        return "Flinkhub User Data Object for User ID: " + user_id;
    }
}
