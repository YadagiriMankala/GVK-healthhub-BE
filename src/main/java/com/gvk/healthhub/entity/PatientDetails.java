package com.gvk.healthhub.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Embeddable
@Data
public class PatientDetails {

    @Column(name = "patient_name", length = 100)
    @NotNull
    private String patientName;

    @Column(name = "patient_phone", length = 10)
    @NotNull
    @Pattern(
        regexp = "^[6-9]\\d{9}$",
        message = "Invalid mobile number"
    )
    private String patientPhone;

    @Column(name = "patient_gender", length = 20)
    @NotNull
    private String patientGender;

}
