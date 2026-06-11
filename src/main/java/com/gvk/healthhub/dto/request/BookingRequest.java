package com.gvk.healthhub.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gvk.healthhub.entity.PatientDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class BookingRequest {

    private Long doctorId;

    private String specialization;

    @NotNull
    private LocalDateTime slotTime;

    @Valid
    private PatientDetails patientDetails;

    private String appointmentType;

    private Long hospitalId;

    @NotNull
    private Double slotPrice;

    private List<String> testNames;

    private Address patientSampleCollectionAddress;
}
