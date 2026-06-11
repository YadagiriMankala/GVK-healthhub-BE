package com.gvk.healthhub.service;

import com.gvk.healthhub.dto.request.BookingRequest;
import com.gvk.healthhub.entity.BookingDetails;
import com.gvk.healthhub.entity.Hospital;
import com.gvk.healthhub.enums.AppointmentType;
import com.gvk.healthhub.enums.BookingStatus;
import com.gvk.healthhub.exception.ResourceNotFoundException;
import com.gvk.healthhub.exception.ValidationException;
import com.gvk.healthhub.repository.BookingDetailsRepository;
import com.gvk.healthhub.repository.DoctorRepository;
import com.gvk.healthhub.repository.HospitalRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BookingService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final BookingDetailsRepository bookingDetailsRepository;

    @Autowired
    public BookingService(DoctorRepository doctorRepository,
            HospitalRepository hospitalRepository,
            BookingDetailsRepository bookingDetailsRepository) {
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.bookingDetailsRepository = bookingDetailsRepository;
    }

    @Transactional
    public void bookAppointment(BookingRequest request) {
        AppointmentType appointmentType;
        try {
            appointmentType = AppointmentType.valueOf(request.getAppointmentType());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid appointment type: " + request.getAppointmentType());
        }

        if (appointmentType == AppointmentType.DOCTOR_APPOINTMENT) {
            bookDoctorAppointment(request);
        } else {
            bookDiagnosticTest(request);
        }
    }

    private void bookDiagnosticTest(BookingRequest request) {
        if (Objects.isNull(request.getTestNames()) || request.getTestNames().isEmpty())
            throw new ValidationException("At least one test is required for diagnostic test booking");

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + request.getHospitalId()));

        BookingDetails bookingDetails = BookingDetails.builder()
                .appointmentType(AppointmentType.DIAGNOSTIC_TEST)
                .testNames(request.getTestNames())
                .slotPrice(request.getSlotPrice())
                .patientDetails(request.getPatientDetails())
                .bookingStatus(BookingStatus.CONFIRMED)
                .hospital(hospital)
                .patientSampleAddress(request.getPatientSampleCollectionAddress())
                .slotTime(request.getSlotTime())
                .build();

        bookingDetailsRepository.save(bookingDetails);
    }

    private void bookDoctorAppointment(BookingRequest request) {
        preValidationForDoctorBooking(request);

        String doctorName = doctorRepository.findDoctorNameById(request.getDoctorId());
        if (Objects.isNull(doctorName))
            throw new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId());

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + request.getHospitalId()));

        BookingDetails bookingDetails = BookingDetails.builder()
                .doctorName(doctorName)
                .appointmentType(AppointmentType.DOCTOR_APPOINTMENT)
                .patientDetails(request.getPatientDetails())
                .slotPrice(request.getSlotPrice())
                .bookingStatus(BookingStatus.CONFIRMED)
                .hospital(hospital)
                .slotTime(request.getSlotTime())
                .specialization(request.getSpecialization())
                .build();

        log.info("Booking details : {}", bookingDetails);

        bookingDetailsRepository.save(bookingDetails);
    }

    private void preValidationForDoctorBooking(BookingRequest request) {
        if (Objects.isNull(request.getDoctorId()))
            throw new ValidationException("Doctor id is required for booking doctor appointment");

        if (Objects.isNull(request.getHospitalId()))
            throw new ValidationException("Hospital id is required for booking doctor appointment");

        if (Objects.isNull(request.getSlotTime()))
            throw new ValidationException("Slot time is required for booking doctor appointment");

        if (Objects.isNull(request.getSpecialization()))
            throw new ValidationException("Specialization is required for booking doctor appointment");
    }
}
