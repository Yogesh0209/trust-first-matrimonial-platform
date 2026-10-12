package com.tfmsp.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tfmsp.backend.enums.Gender;
import com.tfmsp.backend.enums.MaritalStatus;

public class SearchResultResponse {

    private Long profileId;
    private Long userId;
    private String fullName;
    private Gender gender;
    private Integer age;
    private LocalDate dateOfBirth;
    private MaritalStatus maritalStatus;
    private String religion;
    private String city;
    private Integer heightCm;
    private String education;
    private String occupation;
    private String familyType;
    private String about;
    private short trustLevel;
    private String trustBand;
    private int trustScore;
    private int completenessPercentage;
    private String primaryPhotoUrl;
    private String bureauName;
    private boolean shortlisted;
    private LocalDateTime lastActiveAt;

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Integer heightCm) {
        this.heightCm = heightCm;
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

    public String getFamilyType() {
        return familyType;
    }

    public void setFamilyType(String familyType) {
        this.familyType = familyType;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public short getTrustLevel() {
        return trustLevel;
    }

    public void setTrustLevel(short trustLevel) {
        this.trustLevel = trustLevel;
    }

    public String getTrustBand() {
        return trustBand;
    }

    public void setTrustBand(String trustBand) {
        this.trustBand = trustBand;
    }

    public int getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(int trustScore) {
        this.trustScore = trustScore;
    }

    public int getCompletenessPercentage() {
        return completenessPercentage;
    }

    public void setCompletenessPercentage(int completenessPercentage) {
        this.completenessPercentage = completenessPercentage;
    }

    public String getPrimaryPhotoUrl() {
        return primaryPhotoUrl;
    }

    public void setPrimaryPhotoUrl(String primaryPhotoUrl) {
        this.primaryPhotoUrl = primaryPhotoUrl;
    }

    public String getBureauName() {
        return bureauName;
    }

    public void setBureauName(String bureauName) {
        this.bureauName = bureauName;
    }

    public boolean isShortlisted() {
        return shortlisted;
    }

    public void setShortlisted(boolean shortlisted) {
        this.shortlisted = shortlisted;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
