package com.gvk.healthhub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for updating a doctor's details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DoctorUpdateRequest {
    private String title;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String mobile;
    private String qualification;
    private Integer experienceYears;
    private Integer practicingFromYear;
    private String registrationNumber;
    private String designation;
    private String description;
    private String imageUrl;
    private String slugName;
    private BigDecimal consultationFeeWalkin;
    private BigDecimal consultationFeeVideo;
    private Boolean allowsVideoConsultation;
    private Boolean allowsPhoneConsultation;
    private Boolean isFeatured;
    private Boolean isActive;
}
