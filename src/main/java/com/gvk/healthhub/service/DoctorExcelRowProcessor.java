package com.gvk.healthhub.service;

import com.gvk.healthhub.entity.*;
import com.gvk.healthhub.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DoctorExcelRowProcessor {

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final LanguageRepository languageRepository;
    private final HospitalRepository hospitalRepository;

    @Autowired
    public DoctorExcelRowProcessor(DoctorRepository doctorRepository,
                                  SpecializationRepository specializationRepository,
                                  LanguageRepository languageRepository,
                                  HospitalRepository hospitalRepository) {
        this.doctorRepository = doctorRepository;
        this.specializationRepository = specializationRepository;
        this.languageRepository = languageRepository;
        this.hospitalRepository = hospitalRepository;
    }

    /**
     * Processes a single Excel row inside its own isolated transaction boundary.
     * If this method throws an exception, only this row's changes are rolled back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String processRow(Row row, DataFormatter formatter, int rowNum) {
        // 1. Read fields
        Long doctorId = getLongValue(row.getCell(0), formatter);
        String title = getStringValue(row.getCell(1), formatter);
        String firstName = getStringValue(row.getCell(2), formatter);
        String lastName = getStringValue(row.getCell(3), formatter);
        String slugName = getStringValue(row.getCell(4), formatter);
        String gender = getStringValue(row.getCell(5), formatter);
        String email = getStringValue(row.getCell(6), formatter);
        String mobile = getStringValue(row.getCell(7), formatter);
        String qualification = getStringValue(row.getCell(8), formatter);
        Integer experienceYears = getIntegerValue(row.getCell(9), formatter);
        Integer practicingFromYear = getIntegerValue(row.getCell(10), formatter);
        String registrationNumber = getStringValue(row.getCell(11), formatter);
        String designation = getStringValue(row.getCell(12), formatter);
        String description = getStringValue(row.getCell(13), formatter);
        String imageUrl = getStringValue(row.getCell(14), formatter);
        BigDecimal consultationFeeWalkin = getBigDecimalValue(row.getCell(15), formatter);
        BigDecimal consultationFeeVideo = getBigDecimalValue(row.getCell(16), formatter);
        Boolean allowsVideoConsultation = getBooleanValue(row.getCell(17), formatter);
        Boolean allowsPhoneConsultation = getBooleanValue(row.getCell(18), formatter);
        Boolean isFeatured = getBooleanValue(row.getCell(19), formatter);
        Boolean isActive = getBooleanValue(row.getCell(20), formatter);

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First Name is required.");
        }

        // 2. Fetch or Create Doctor
        Doctor doctor;
        boolean isUpdate = false;
        if (doctorId != null) {
            // An ID was supplied — it MUST be an existing doctor.
            // We never allow the admin to choose/assign a manual ID.
            doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Doctor ID " + doctorId + " does not exist. Leave the Doctor ID cell empty to create a new doctor."));
            isUpdate = true;
        } else {
            // No ID supplied — create a brand-new doctor
            doctor = new Doctor();
        }

        // Update fields
        doctor.setTitle(title);
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setGender(gender);
        doctor.setEmail(email);
        doctor.setMobile(mobile);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experienceYears);
        doctor.setPracticingFromYear(practicingFromYear);
        doctor.setRegistrationNumber(registrationNumber);
        doctor.setDesignation(designation);
        doctor.setDescription(description);
        doctor.setImageUrl(imageUrl);
        doctor.setConsultationFeeWalkin(consultationFeeWalkin);
        doctor.setConsultationFeeVideo(consultationFeeVideo);
        doctor.setAllowsVideoConsultation(allowsVideoConsultation != null ? allowsVideoConsultation : false);
        doctor.setAllowsPhoneConsultation(allowsPhoneConsultation != null ? allowsPhoneConsultation : false);
        doctor.setIsFeatured(isFeatured != null ? isFeatured : false);
        doctor.setIsActive(isActive != null ? isActive : true);

        // 3. Handle unique slug logic
        if (slugName != null && !slugName.trim().isEmpty()) {
            doctor.setSlug(slugName.trim().toLowerCase());
        } else if (!isUpdate || doctor.getSlug() == null) {
            String baseSlug = (firstName + "-" + (lastName != null ? lastName : ""))
                    .toLowerCase()
                    .replaceAll("[^a-z0-9\\-]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
            if (baseSlug.isEmpty()) {
                baseSlug = "doctor";
            }
            String finalSlug = baseSlug;
            int count = 1;
            // Prevent slug duplication
            while (doctorRepository.existsBySlug(finalSlug)) {
                finalSlug = baseSlug + "-" + count++;
            }
            doctor.setSlug(finalSlug);
        }

        // Save parent doctor first to guarantee database ID
        doctor = doctorRepository.saveAndFlush(doctor);

        // 4. Resolve Specializations (up to 2 cols)
        List<String> specNames = new ArrayList<>();
        addIfNotEmpty(specNames, getStringValue(row.getCell(21), formatter));
        addIfNotEmpty(specNames, getStringValue(row.getCell(22), formatter));

        // Clear existing specializations
        if (doctor.getDoctorSpecializations() != null) {
            doctor.getDoctorSpecializations().clear();
        } else {
            doctor.setDoctorSpecializations(new ArrayList<>());
        }

        for (String name : specNames) {
            Specialization spec = specializationRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        // Dynamically assign manually controlled next increment ID
                        Long nextId = specializationRepository.findMaxId() + 1;
                        Specialization newSpec = new Specialization(nextId, name);
                        return specializationRepository.saveAndFlush(newSpec);
                    });

            DoctorSpecialization docSpec = new DoctorSpecialization(null, doctor, spec);
            doctor.getDoctorSpecializations().add(docSpec);
        }

        // 5. Resolve Languages (up to 3 cols)
        List<String> langNames = new ArrayList<>();
        addIfNotEmpty(langNames, getStringValue(row.getCell(23), formatter));
        addIfNotEmpty(langNames, getStringValue(row.getCell(24), formatter));
        addIfNotEmpty(langNames, getStringValue(row.getCell(25), formatter));

        // Clear existing languages
        if (doctor.getDoctorLanguages() != null) {
            doctor.getDoctorLanguages().clear();
        } else {
            doctor.setDoctorLanguages(new ArrayList<>());
        }

        for (String name : langNames) {
            Language lang = languageRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Long nextId = languageRepository.findMaxId() + 1;
                        Language newLang = new Language(nextId, name);
                        return languageRepository.saveAndFlush(newLang);
                    });

            DoctorLanguage docLang = new DoctorLanguage(null, doctor, lang);
            doctor.getDoctorLanguages().add(docLang);
        }

        // 6. Resolve Hospitals (up to 2 cols) — auto-create stub if not found
        List<String> hospNames = new ArrayList<>();
        addIfNotEmpty(hospNames, getStringValue(row.getCell(26), formatter));
        addIfNotEmpty(hospNames, getStringValue(row.getCell(27), formatter));

        // Clear existing hospital assignments
        if (doctor.getDoctorHospitals() != null) {
            doctor.getDoctorHospitals().clear();
        } else {
            doctor.setDoctorHospitals(new ArrayList<>());
        }

        for (String name : hospNames) {
            Hospital hospital = hospitalRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        // Auto-create a minimal hospital stub with just the name
                        Long nextId = hospitalRepository.findMaxId() + 1;
                        Hospital newHospital = new Hospital();
                        newHospital.setId(nextId);
                        newHospital.setName(name);
                        return hospitalRepository.saveAndFlush(newHospital);
                    });

            DoctorHospital docHosp = new DoctorHospital(
                    null,
                    doctor,
                    hospital,
                    doctor.getConsultationFeeWalkin(),
                    doctor.getConsultationFeeVideo()
            );
            doctor.getDoctorHospitals().add(docHosp);
        }

        // Re-save doctor details with relationship mappings cascading
        doctorRepository.saveAndFlush(doctor);

        return "Row " + rowNum + ": " + (isUpdate ? "Updated" : "Created") + " doctor "
                + doctor.getTitle() + " " + doctor.getFirstName() + " " + doctor.getLastName()
                + " (ID: " + doctor.getId() + ")";
    }

    private void addIfNotEmpty(List<String> list, String value) {
        if (value != null && !value.trim().isEmpty()) {
            list.add(value.trim());
        }
    }

    // Safely reads Cell as String
    private String getStringValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        String val = formatter.formatCellValue(cell).trim();
        return val.isEmpty() ? null : val;
    }

    // Safely reads Cell as Integer
    private Integer getIntegerValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as Long
    private Long getLongValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return (long) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as BigDecimal
    private BigDecimal getBigDecimalValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Safely reads Cell as Boolean
    private Boolean getBooleanValue(Cell cell, DataFormatter formatter) {
        String val = getStringValue(cell, formatter);
        if (val == null) return null;
        return "TRUE".equalsIgnoreCase(val) || "YES".equalsIgnoreCase(val) || "Y".equalsIgnoreCase(val);
    }
}
