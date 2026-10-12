package com.tfmsp.backend.dto;

import com.tfmsp.backend.enums.MaritalStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileRequest {

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @Size(max = 50, message = "Religion cannot exceed 50 characters")
    private String religion;

    @NotBlank(message = "Mother tongue is required")
    @Size(max = 50, message = "Mother tongue cannot exceed 50 characters")
    private String motherTongue;

    @NotBlank(message = "City is required")
    @Size(max = 80, message = "City cannot exceed 80 characters")
    private String city;

    @Min(value = 100, message = "Height must be at least 100 cm")
    @Max(value = 250, message = "Height cannot exceed 250 cm")
    private Integer heightCm;

    @NotBlank(message = "Education is required")
    @Size(max = 100, message = "Education cannot exceed 100 characters")
    private String education;

    @NotBlank(message = "Occupation is required")
    @Size(max = 100, message = "Occupation cannot exceed 100 characters")
    private String occupation;

    @Size(max = 30, message = "Family type cannot exceed 30 characters")
    private String familyType;

    @Size(max = 1000, message = "About text cannot exceed 1000 characters")
    private String about;

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

    public String getMotherTongue() {
        return motherTongue;
    }

    public void setMotherTongue(String motherTongue) {
        this.motherTongue = motherTongue;
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
}
