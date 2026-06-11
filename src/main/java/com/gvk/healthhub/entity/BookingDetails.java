package com.gvk.healthhub.entity;

import com.gvk.healthhub.dto.request.Address;
import com.gvk.healthhub.enums.AppointmentType;
import com.gvk.healthhub.enums.BookingStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


@Entity
@Table(
    name = "t_booking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name")
    private String doctorName;

    @Embedded
    private PatientDetails patientDetails;

    @Column(name = "slot_time")
    private LocalDateTime slotTime;

    @Column(name = "slot_price")
    private Double slotPrice;

    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @Embedded
    private Address patientSampleAddress;

    @Column(name = "specialization")
    private String specialization;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_booking_test_names", joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "test_name")
    @Builder.Default
    private List<String> testNames= new ArrayList<>();;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Column(name = "appointment_type")
    @Enumerated(EnumType.STRING)
    private AppointmentType appointmentType;

}
