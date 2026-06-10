package com.gvk.healthhub.service;

import com.gvk.healthhub.dto.request.DoctorListRequest;
import com.gvk.healthhub.dto.request.DoctorUpdateRequest;
import com.gvk.healthhub.dto.response.ApiResponse;
import com.gvk.healthhub.dto.response.DoctorDTO;
import com.gvk.healthhub.entity.Doctor;
import com.gvk.healthhub.mapper.DoctorMapper;
import com.gvk.healthhub.mapper.DoctorUpdateMapper;
import com.gvk.healthhub.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;
    private final DoctorUpdateMapper doctorUpdateMapper;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, DoctorMapper doctorMapper, DoctorUpdateMapper doctorUpdateMapper) {
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
        this.doctorUpdateMapper = doctorUpdateMapper;
    }

    /**
     * Get all active doctors with pagination
     */
    public ApiResponse<List<DoctorDTO>> getAllDoctors(Pageable pageable) {
        try {
            Page<Doctor> doctorPage = doctorRepository.findByIsActiveTrue(pageable);
            List<DoctorDTO> doctorDTOs = doctorMapper.toDTOList(doctorPage.getContent());
            return ApiResponse.success("Doctors retrieved successfully", doctorDTOs);
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve doctors: " + e.getMessage());
        }
    }

//    /**
//     * Get doctors by specialization with pagination
//     */
//    public ApiResponse<List<DoctorDTO>> getDoctorsBySpecialization(String specialization, Pageable pageable) {
//        try {
//            List<Doctor> doctors = doctorRepository.findAll().stream()
//                    .filter(d -> d.getIsActive() && 
//                           specialization != null && 
//                           d.getSpecialization() != null &&
//                           d.getSpecialization().equalsIgnoreCase(specialization))
//                    .skip(pageable.getOffset())
//                    .limit(pageable.getPageSize())
//                    .collect(Collectors.toList());
//            
//            List<DoctorDTO> doctorDTOs = doctorMapper.toDTOList(doctors);
//            return ApiResponse.success("Doctors by specialization retrieved successfully", doctorDTOs);
//        } catch (Exception e) {
//            return ApiResponse.error("Failed to retrieve doctors by specialization: " + e.getMessage());
//        }
//    }

    /**
     * Get featured doctors
     */
    public ApiResponse<List<DoctorDTO>> getFeaturedDoctors() {
        try {
            List<Doctor> doctors = doctorRepository.findFeaturedDoctors();
            List<DoctorDTO> doctorDTOs = doctorMapper.toDTOList(doctors);
            return ApiResponse.success("Featured doctors retrieved successfully", doctorDTOs);
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve featured doctors: " + e.getMessage());
        }
    }

    /**
     * Get doctor by slug
     */
    public ApiResponse<DoctorDTO> getDoctorBySlug(String slug) {
        try {
            return doctorRepository.findBySlug(slug)
                    .map(doctor -> ApiResponse.success(doctorMapper.toDTO(doctor)))
                    .orElse(ApiResponse.error("Doctor not found with slug: " + slug, 404));
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve doctor: " + e.getMessage());
        }
    }

    /**
     * Search doctors with multiple filters
     */
    @Transactional
    public ApiResponse<Page<DoctorDTO>> searchDoctors(
            DoctorListRequest request,
            Pageable pageable) {

        List<Long> serviceIdList = request.getServiceIdList();
        boolean serviceIdsEmpty = (serviceIdList == null || serviceIdList.isEmpty());
        if (serviceIdsEmpty) {
            serviceIdList = List.of(-1L);
        }

        List<Long> pocIdList = request.getPocIdList();
        boolean pocIdsEmpty = (pocIdList == null || pocIdList.isEmpty());
        if (pocIdsEmpty) {
            pocIdList = List.of(-1L);
        }

        Page<Doctor> doctors =
                doctorRepository.searchDoctors(
                        request.getServiceId(),
                        serviceIdList,
                        serviceIdsEmpty,
                        pocIdList,
                        pocIdsEmpty,
                        request.getMinExperience(),
                        request.getMaxExperience(),
                        pageable);

        return ApiResponse.success(
                "Doctors fetched successfully",
                doctors.map(doctorMapper::toDTO));
    }

    /**
     * Update doctor details
     */
    @Transactional
    public ApiResponse<DoctorDTO> updateDoctor(Long id, DoctorUpdateRequest request) {
        try {
            return doctorRepository.findById(id)
                    .map(doctor -> {
                        // Map properties dynamically, ignoring null source values (Patch operation)
                        doctorUpdateMapper.updateDoctorFromDto(request, doctor);

                        // Handle Slug update
                        if (request.getSlugName() != null && !request.getSlugName().trim().isEmpty()) {
                            doctor.setSlug(request.getSlugName().trim());
                        } else if (request.getFirstName() != null || request.getLastName() != null) {
                            String baseSlug = ((doctor.getFirstName() != null ? doctor.getFirstName() : "") + "-" +
                                    (doctor.getLastName() != null ? doctor.getLastName() : ""))
                                    .toLowerCase()
                                    .replaceAll("[^a-z0-9\\-]", "-")
                                    .replaceAll("-+", "-")
                                    .replaceAll("^-|-$", "");
                            if (baseSlug.isEmpty()) {
                                baseSlug = "doctor-" + doctor.getId();
                            }
                            
                            // Prevent unique constraint violation
                            String finalSlug = baseSlug;
                            int count = 1;
                            while (doctorRepository.existsBySlug(finalSlug)) {
                                finalSlug = baseSlug + "-" + count++;
                            }
                            doctor.setSlug(finalSlug);
                        }

                        Doctor saved = doctorRepository.save(doctor);
                        return ApiResponse.success("Doctor details updated successfully", doctorMapper.toDTO(saved));
                    })
                    .orElseGet(() -> ApiResponse.error("Doctor not found with ID: " + id, 404));
        } catch (Exception e) {
            return ApiResponse.error("Failed to update doctor: " + e.getMessage());
        }
    }
}