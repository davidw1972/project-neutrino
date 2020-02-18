package org.flinkhub.fhnet.models;

public class Company {
    private String id = "";
    private String name = "";
    private String domainName = "";

    public Company() {
    }

    public Company(String id, String name, String domainName) {
        this.id = id;
        this.name = name;
        this.domainName = domainName;
    }

    public Company(String name, String domainName) {
        this.name = name;
        this.domainName = domainName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getCompanyName() {
        if (name.length() > 0) {
            return name;
        }

        if (domainName.length() > 0) {
            return domainName;
        }

        return "Unknown";
    }
}
