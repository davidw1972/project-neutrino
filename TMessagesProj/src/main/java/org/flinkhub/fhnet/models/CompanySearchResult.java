package org.flinkhub.fhnet.models;

import java.util.List;
import java.util.Vector;

public class CompanySearchResult {
    private String queryStr = "";
    private List<Company> companies = new Vector<>();
    private boolean isCorrect = false;
    private String errorMessage = "";

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
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
}
