package org.flinkhub.fhnet.models;

import org.joda.time.DateTime;

public class Experience {
    public static int EDUCATION = 1;
    public static int WORK_EXP = 2;

    private String id = "";
    private int experienceType = 0;
    private Company company;
    private String degreeStr = "";
    private String roleStr = "";
    private String description = "";
    private DateTime startDate = null;
    private DateTime endDate = null;
    private boolean isCurrent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getExperienceType() {
        return experienceType;
    }

    public void setExperienceType(int experienceType) {
        this.experienceType = experienceType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDegreeStr() {
        return degreeStr;
    }

    public void setDegreeStr(String degreeStr) {
        this.degreeStr = degreeStr;
    }

    public String getRoleStr() {
        return roleStr;
    }

    public void setRoleStr(String roleStr) {
        this.roleStr = roleStr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
