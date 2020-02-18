package org.flinkhub.fhnet;

import android.util.Log;

import org.flinkhub.fhnet.models.Company;
import org.flinkhub.fhnet.models.CompanySearchResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class CompanyData {
    private static HashMap<String, CompanySearchResult> searchCache = new HashMap<>();

    private static CompanySearchResult parseCompanySearchResponse(String response) throws JSONException {
        CompanySearchResult result = new CompanySearchResult();

        JSONObject j = new JSONObject(response);
        if (!j.has("success") || !j.getBoolean("success")) {
            result.setCorrect(false);
            String errorMessage = j.has("error") ? j.getString("error") : "";
            result.setErrorMessage(errorMessage);

            return result;
        }

        List<Company> companies = new Vector<>();
        JSONArray companiesArr = j.getJSONArray("results");
        for (int i = 0; i < companiesArr.length(); i++) {
            Company c = new Company();
            JSONObject obj = companiesArr.getJSONObject(i);

            c.setId(obj.optString("_id", ""));
            c.setName(obj.optString("name", ""));
            c.setDomainName(obj.optString("domainName", ""));

            companies.add(c);
        }

        result.setCompanies(companies);
        result.setCorrect(true);
        return result;
    }

    public static CompanySearchResult getCompanies(String query, String industryTags) {
        if (searchCache.containsKey(query)) {
            return searchCache.get(query);
        }

        CompanySearchResult result;

        JSONObject postData = new JSONObject();
        try {
            postData.put("q", query);
            postData.put("page", 1);
            postData.put("limit", 10);
            postData.put("industryTags", industryTags);
        } catch (JSONException ex) {
            // TODO Handle json error
        }

        try {
            String response = WebRequest.post(Constants.SEARCH_COMPANIES_URL, postData.toString());
            result = parseCompanySearchResponse(response);
            result.setQueryStr(query);
            result.setCorrect(true);

            searchCache.put(query, result);
        } catch (JSONException ex) {
            // TODO Handle JSON Parsing failure
            Log.e(Constants.TAG, "JSON Parsing failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new CompanySearchResult();
            result.setQueryStr(query);
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        } catch (IOException ex) {
            // TODO Handle request failure
            Log.e(Constants.TAG, "Request failed");
            Log.e(Constants.TAG, ex.getMessage());

            result = new CompanySearchResult();
            result.setQueryStr(query);
            result.setCorrect(false);
            result.setErrorMessage("Bad response from server");
        }

        return result;
    }

    public static CompanySearchResult getUniversities(String query) {
        return getCompanies(query, "University,Institute");
    }
}
