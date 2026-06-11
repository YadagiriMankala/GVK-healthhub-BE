package com.gvk.healthhub.controller;

import com.gvk.healthhub.dto.request.DoctorListRequest;
import com.gvk.healthhub.dto.request.DoctorUpdateRequest;
import com.gvk.healthhub.dto.response.ApiResponse;
import com.gvk.healthhub.dto.response.DoctorDTO;
import com.gvk.healthhub.dto.response.HospitalDTO;
import com.gvk.healthhub.entity.Hospital;
import com.gvk.healthhub.repository.HospitalRepository;
import com.gvk.healthhub.service.DoctorExcelService;
import com.gvk.healthhub.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Doctor Controller - Matches existing frontend API integration Base path:
 * /POZAppServices (to match existing API structure)
 */
@RestController
@RequestMapping("/POZAppServices")
@Tag(name = "Doctor Management", description = "APIs for managing and retrieving doctor information")
public class DoctorController {

	private final DoctorService doctorService;
	private final HospitalRepository hospitalRepository;
	private final DoctorExcelService doctorExcelService;

	@Autowired
	public DoctorController(DoctorService doctorService,
                            HospitalRepository hospitalRepository,
                            DoctorExcelService doctorExcelService) {
		this.doctorService = doctorService;
		this.hospitalRepository = hospitalRepository;
		this.doctorExcelService = doctorExcelService;
	}

	/**
     * Get Doctor List - Matches existing frontend API: POZAppServices/consumercontroller/getdoctorlist
     * POST endpoint with JSON request body
     */
    @PostMapping("/consumercontroller/getdoctorlist")
    @Operation(
            summary = "Get doctor list",
            description = "Retrieve list of doctors matching frontend format"
    )
    public ResponseEntity<List<DoctorDTO>> getDoctorList(
            @RequestBody DoctorListRequest request) {

        Pageable pageable = PageRequest.of(
                request.getFrom() != null ? request.getFrom() : 0,
                request.getSize() != null ? request.getSize() : 20
        );

        ApiResponse<Page<DoctorDTO>> serviceResponse =
                doctorService.searchDoctors(
                        request,
                        pageable
                );

        if (serviceResponse.isSuccess() && serviceResponse.getData() != null) {
            Page<DoctorDTO> page = serviceResponse.getData();
            return ResponseEntity.ok(page.getContent());
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Update Doctor - PUT POZAppServices/doctorappcontrol/updatedoctor/{id}
     */
    @PutMapping("/doctorappcontrol/updatedoctor/{id}")
    @Operation(
            summary = "Update doctor details",
            description = "Update details of an existing doctor by their ID"
    )
    public ResponseEntity<ApiResponse<DoctorDTO>> updateDoctor(
            @Parameter(description = "Doctor's unique ID") @PathVariable Long id,
            @RequestBody DoctorUpdateRequest request) {
        ApiResponse<DoctorDTO> response = doctorService.updateDoctor(id, request);
        if (response.getStatusCode() != null && response.getStatusCode() == 404) {
            return ResponseEntity.status(404).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Export Doctors - GET POZAppServices/doctorappcontrol/export
     */
    @GetMapping("/doctorappcontrol/export")
    @Operation(
            summary = "Export doctors to Excel",
            description = "Download an Excel spreadsheet of all doctors with drop-down data validation"
    )
    public ResponseEntity<InputStreamResource> exportDoctors() {
        ByteArrayInputStream in = doctorExcelService.exportDoctorsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=doctors.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    /**
     * Import Doctors - POST POZAppServices/doctorappcontrol/import
     */
    @PostMapping(value = "/doctorappcontrol/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import doctors from Excel",
            description = "Upload an Excel spreadsheet to bulk create or update doctor records"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> importDoctors(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> report = doctorExcelService.importDoctorsFromExcel(file.getInputStream());
            return ResponseEntity.ok(ApiResponse.success("Excel import processed successfully", report));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to process Excel import: " + e.getMessage()));
        }
    }

	/**
	 * Get Doctor Details - Matches existing frontend API:
	 * POZAppServices/doctorappcontrol/getdoctordetails Query param:
	 * slugName=doctor-slug
	 */
	@GetMapping("/doctorappcontrol/getdoctordetails")
	@Operation(summary = "Get doctor details", description = "Retrieve detailed information about a specific doctor by slug")
	public ResponseEntity<ApiResponse<DoctorDTO>> getDoctorDetails(
			@Parameter(description = "Doctor's unique slug identifier") @RequestParam String slugName) {
		ApiResponse<DoctorDTO> response = doctorService.getDoctorBySlug(slugName);
		if (response.getStatusCode() != null && response.getStatusCode() == 404) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	/**
	 * Get Doctor POCs - Matches existing frontend API:
	 * POZAppServices/schedulecontrol/getpocsofdoctor
	 */
	@GetMapping("/schedulecontrol/getpocsofdoctor")
	@Operation(summary = "Get POCs of doctor", description = "Retrieve Points of Care (hospitals/clinics) for a specific doctor")
	public ResponseEntity<ApiResponse<Object>> getPocsOfDoctor(
			@Parameter(description = "Doctor ID") @RequestParam Long doctorId) {
		// TODO: Implement POC/Hospital mapping when Hospital entity is added
		ApiResponse<Object> response = ApiResponse.success("POCs retrieved successfully", List.of());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/doctorappcontrol/hospitalList")
	@Operation(summary = "Get hospital list", description = "Retrieve hospital list by slug")
	public ResponseEntity<ApiResponse<List<HospitalDTO>>> getHospitalList(
			@Parameter(description = "Hospital slug") @RequestParam(name = "pocSlug", required = false) String pocSlug) {
		
		List<Hospital> hospitals = hospitalRepository.findAll();
		List<HospitalDTO> dtos = hospitals.stream()
				.map(h -> {
					HospitalDTO.AddressDTO address = HospitalDTO.AddressDTO.builder()
							.address1(h.getAddressLine1())
							.address2("")
							.locality(h.getLocality())
							.cityName(h.getCity())
							.stateName(h.getState())
							.pinCode(h.getPincode())
							.build();
							
					return HospitalDTO.builder()
							.pocId(h.getId())
							.pocName(h.getName())
							.pocSlug(pocSlug != null ? pocSlug : "gvk-health")
							.address(address)
							.contactList(h.getPhone() != null ? List.of(h.getPhone()) : List.of())
							.email("gvk@hs.com")
							.build();
				})
				.collect(Collectors.toList());
				
		ApiResponse<List<HospitalDTO>> response = ApiResponse.success("Hospital list retrieved successfully", dtos);
		return ResponseEntity.ok(response);
	}
}