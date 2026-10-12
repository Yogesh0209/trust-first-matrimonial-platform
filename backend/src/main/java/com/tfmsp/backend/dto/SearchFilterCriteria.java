package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.MaritalStatus;

public class SearchFilterCriteria {

    private Gender gender;
    private Integer minAge;
    private Integer maxAge;
    private String religion;
    private MaritalStatus maritalStatus;
    private String city;
    private String education;
    private String occupation;
    private Integer minTrustLevel;
    private String trustBand;
    private Integer minTrustScore;

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public Integer getMinTrustLevel() {
        return minTrustLevel;
    }

    public void setMinTrustLevel(Integer minTrustLevel) {
        this.minTrustLevel = minTrustLevel;
    }

    public String getTrustBand() {
        return trustBand;
    }

    public void setTrustBand(String trustBand) {
        this.trustBand = trustBand;
    }

    public Integer getMinTrustScore() {
        return minTrustScore;
    }

    public void setMinTrustScore(Integer minTrustScore) {
        this.minTrustScore = minTrustScore;
    }
}
