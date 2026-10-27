package com.sinst.model;

import java.util.Objects;

public class Company {
    private int companyId;
    private String companyName;
    private String industry;
    private String location;
    private String website;
    private String careersUrl;
    private String contactEmail;

    public Company() {}

    public Company(int companyId, String companyName, String industry, String location, String website, String contactEmail) {
        this(companyId, companyName, industry, location, website, website, contactEmail);
    }

    public Company(int companyId, String companyName, String industry, String location, String website, String careersUrl, String contactEmail) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.website = website;
        this.careersUrl = careersUrl;
        this.contactEmail = contactEmail;
    }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCareersUrl() { return careersUrl; }
    public void setCareersUrl(String careersUrl) { this.careersUrl = careersUrl; }

    public String getEffectiveCareersUrl() {
        if (careersUrl != null && !careersUrl.trim().isEmpty()) {
            return careersUrl.trim();
        }
        return website != null ? website.trim() : "";
    }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        if (companyId != 0 && company.companyId != 0) return companyId == company.companyId;
        return companyName != null && companyName.equalsIgnoreCase(company.companyName);
    }

    @Override
    public int hashCode() {
        return companyId != 0 ? Integer.hashCode(companyId) : (companyName != null ? companyName.toLowerCase().hashCode() : 0);
    }

    @Override
    public String toString() {
        return companyName;
    }
}
