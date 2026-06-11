package com.gvk.healthhub.service;

import com.gvk.healthhub.dto.request.DiagnosticTestListRequest;
import com.gvk.healthhub.dto.request.DiagnosticTestUpdateRequest;
import com.gvk.healthhub.dto.request.DiagnosticPocRequest;
import com.gvk.healthhub.dto.response.ApiResponse;
import com.gvk.healthhub.dto.response.DiagnosticCategoryDTO;
import com.gvk.healthhub.dto.response.DiagnosticTestDTO;
import com.gvk.healthhub.dto.response.DiagnosticTestDTO.OrderPriceDetails;
import com.gvk.healthhub.dto.response.DiagnosticPocDTO;
import com.gvk.healthhub.entity.DiagnosticCategory;
import com.gvk.healthhub.entity.DiagnosticTest;
import com.gvk.healthhub.entity.Hospital;
import com.gvk.healthhub.repository.DiagnosticCategoryRepository;
import com.gvk.healthhub.repository.DiagnosticTestRepository;
import com.gvk.healthhub.repository.HospitalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Diagnostic Test Catalog APIs.
 *
 * Handles:
 *   GET  /POZAppServices/investigationcontrol/getdiagnosticscategory
 *   POST /POZAppServices/investigationcontrol/tests
 *   GET  /POZAppServices/investigationcontrol/packagetestdetails
 *
 * All mappers (Entity → DTO) produce output that matches the REAL
 * production API JSON verified on 2026-06-10.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class DiagnosticService {

    private final DiagnosticCategoryRepository categoryRepository;
    private final DiagnosticTestRepository testRepository;
    private final HospitalRepository hospitalRepository;

    @Autowired
    public DiagnosticService(DiagnosticCategoryRepository categoryRepository,
                             DiagnosticTestRepository testRepository,
                             HospitalRepository hospitalRepository) {
        this.categoryRepository = categoryRepository;
        this.testRepository = testRepository;
        this.hospitalRepository = hospitalRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1. GET DIAGNOSTIC CATEGORIES
    //     Endpoint: GET /investigationcontrol/getdiagnosticscategory
    //     Params:   brandId, homeCollections, pinCode, categoryId
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns a list of top-level diagnostic categories each with their
     * subServiceList populated.
     * The FE TestController.tsx iterates subServiceList to build filter chips.
     */
    public ApiResponse<List<DiagnosticCategoryDTO>> getDiagnosticsCategories(
            Long brandId,
            Boolean homeCollections,
            String pinCode,
            Long categoryId) {

        try {
            // homeCollections=false → show ALL categories (no filter)
            // homeCollections=true  → only categories available for home collection
            Boolean homeCollectionsFilter = Boolean.TRUE.equals(homeCollections) ? Boolean.TRUE : null;

            List<DiagnosticCategory> categories =
                    categoryRepository.findTopLevelCategories(brandId, homeCollectionsFilter, categoryId);

            List<DiagnosticCategoryDTO> dtos = categories.stream()
                    .map(this::toCategoryDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Categories retrieved successfully", dtos);
        } catch (Exception e) {
            log.error("[DiagnosticService] getDiagnosticsCategories failed: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve diagnostic categories: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2. POST TESTS LIST
    //     Endpoint: POST /investigationcontrol/tests
    //     Body:     DiagnosticTestListRequest
    //
    //  Real request body:
    //    { "pinCode":"","from":0,"size":50,"brandId":55,
    //      "departmentIdList1":[],"departmentIdList2":[],
    //      "departmentIdList3":[],"departmentIdList4":[],
    //      "patientProfileId":0,"parentProfileId":0,
    //      "isWellness":false,"pocId":3717,"categoryId":0,
    //      "searchTerm":"","sortBy":"popular","homeCollections":false }
    // ─────────────────────────────────────────────────────────────────────────

    public ApiResponse<List<DiagnosticTestDTO>> getTests(DiagnosticTestListRequest request) {

        try {
            // ---- Pagination ----
            // 'from' is an offset (like Elasticsearch), NOT a page number.
            // Real API: from=0 → first 50, from=50 → next 50, etc.
            int from = (request.getFrom() != null && request.getFrom() >= 0) ? request.getFrom() : 0;
            int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 50;
            // Convert offset to Spring page number: pageNumber = from / size
            int pageNumber = (size > 0) ? from / size : 0;
            Pageable pageable = PageRequest.of(pageNumber, size);

            // ---- homeCollections filter logic ----
            // FE sends boolean: false = "show all tests" (NO filter applied)
            //                   true  = "home collection only" (filter to homeCollections=1)
            // This matches production API: homeCollections=false returns ALL tests
            Integer homeCollectionsInt = null;
            if (Boolean.TRUE.equals(request.getHomeCollections())) {
                homeCollectionsInt = 1; // only home-collection-enabled tests
            }
            // If false or null → homeCollectionsInt stays null → no filter → returns all tests

            // ---- Department filter lists ----
            List<Long> dept1 = nullSafeList(request.getDepartmentIdList1());
            List<Long> dept2 = nullSafeList(request.getDepartmentIdList2());
            boolean dept1Empty = dept1.isEmpty();
            boolean dept2Empty = dept2.isEmpty();

            // Sentinel for empty department lists (IN clause needs at least one value)
            if (dept1.isEmpty()) dept1 = List.of(-1L);
            if (dept2.isEmpty()) dept2 = List.of(-1L);

            // ---- ServiceId list ----
            List<Long> serviceIdList = nullSafeList(request.getServiceIdList());
            boolean serviceIdsEmpty = serviceIdList.isEmpty();
            if (serviceIdsEmpty) serviceIdList = List.of(-1L);

            // ---- Sort ----
            String sortBy = (request.getSortBy() != null && !request.getSortBy().isBlank())
                    ? request.getSortBy() : "popular";

            Page<DiagnosticTest> page = testRepository.searchTests(
                    request.getBrandId(),
                    request.getPocId(),
                    request.getCategoryId(),
                    homeCollectionsInt,
                    request.getSearchTerm(),
                    dept1, dept1Empty,
                    dept2, dept2Empty,
                    serviceIdList, serviceIdsEmpty,
                    false,   // popularOnly — not a field in real FE request
                    sortBy,
                    pageable
            );

            List<DiagnosticTestDTO> dtos = page.getContent().stream()
                    .map(this::toTestDTO)
                    .collect(Collectors.toList());

            return ApiResponse.success("Tests fetched successfully", dtos);

        } catch (Exception e) {
            log.error("[DiagnosticService] getTests failed: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve tests: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. GET TEST / PACKAGE DETAILS
    //     Endpoint: GET /investigationcontrol/packagetestdetails
    //     Params:   slugName (primary), serviceId (fallback)
    // ─────────────────────────────────────────────────────────────────────────

    public ApiResponse<DiagnosticTestDTO> getTestDetails(String slugName, Long serviceId) {
        try {
            // Try slugName first, then fall back to serviceId
            return testRepository.findBySlugNameAndIsActiveTrue(slugName)
                    .or(() -> (serviceId != null && serviceId > 0)
                            ? testRepository.findByServiceIdAndIsActiveTrue(serviceId)
                            : java.util.Optional.empty())
                    .map(test -> ApiResponse.success("Test details retrieved successfully", toTestDTO(test)))
                    .orElse(ApiResponse.error("Test not found: slugName=" + slugName, 404));
        } catch (Exception e) {
            return ApiResponse.error("Failed to retrieve test details: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  4. UPDATE TEST
    //     Endpoint: PATCH /investigationcontrol/tests/{serviceId}
    //     Body:     DiagnosticTestUpdateRequest (partial — only non-null fields applied)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ApiResponse<DiagnosticTestDTO> updateTest(Long serviceId, DiagnosticTestUpdateRequest req) {
        try {
            DiagnosticTest test = testRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Test not found: serviceId=" + serviceId));

            // ---- Basic Info ----
            if (req.getServiceName()  != null) test.setServiceName(req.getServiceName());
            if (req.getSlugName()     != null) test.setSlugName(req.getSlugName());
            if (req.getTags()         != null) test.setTags(req.getTags());
            if (req.getPrecaution()   != null) test.setPrecaution(req.getPrecaution());

            // ---- Pricing ----
            if (req.getGrossPrice()    != null) test.setGrossPrice(req.getGrossPrice());
            if (req.getDiscountPrice() != null) test.setDiscountPrice(req.getDiscountPrice());
            if (req.getNetPrice()      != null) test.setNetPrice(req.getNetPrice());

            // ---- Category ----
            // e.g. move a lab test (3) → package (4) or just change sub-category
            if (req.getCategoryId()   != null) test.setCategoryId(req.getCategoryId());
            if (req.getCategoryName() != null) test.setCategoryName(req.getCategoryName());
            if (req.getDiagnosticCategoryId() != null) {
                categoryRepository.findById(req.getDiagnosticCategoryId())
                        .ifPresent(test::setDiagnosticCategory);
            }

            // ---- Status ----
            if (req.getIsActive()       != null) test.setIsActive(req.getIsActive());
            if (req.getIsPopular()      != null) test.setIsPopular(req.getIsPopular());
            if (req.getHomeCollections() != null) test.setHomeCollections(req.getHomeCollections());

            // ---- SEO ----
            if (req.getMetaTitle()       != null) test.setMetaTitle(req.getMetaTitle());
            if (req.getMetaDescription() != null) test.setMetaDescription(req.getMetaDescription());

            DiagnosticTest saved = testRepository.save(test);
            log.info("[DiagnosticService] updateTest — serviceId={} updated successfully", serviceId);
            return ApiResponse.success("Test updated successfully", toTestDTO(saved));

        } catch (RuntimeException e) {
            log.warn("[DiagnosticService] updateTest — not found: {}", e.getMessage());
            return ApiResponse.error(e.getMessage(), 404);
        } catch (Exception e) {
            log.error("[DiagnosticService] updateTest failed: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to update test: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  5. GET DIAGNOSTIC POC LIST
    //     Endpoint: POST /consumercontroller/diagnosticpoc
    //     Body:     DiagnosticPocRequest
    // ─────────────────────────────────────────────────────────────────────────
    public ApiResponse<List<DiagnosticPocDTO>> getDiagnosticPocs(DiagnosticPocRequest request) {
        try {
            List<Hospital> hospitals = hospitalRepository.findAll();

            // Optionally filter by pincode if requested and there is any match
            if (request.getPinCode() != null && !request.getPinCode().isBlank()) {
                List<Hospital> filtered = hospitals.stream()
                        .filter(h -> h.getPincode() != null && h.getPincode().trim().equals(request.getPinCode().trim()))
                        .collect(Collectors.toList());
                if (!filtered.isEmpty()) {
                    hospitals = filtered;
                }
            }

            // Convert to DTOs matching the TS model PocDetails structure
            List<DiagnosticPocDTO> dtos = hospitals.stream()
                    .map(h -> {
                        DiagnosticPocDTO.AddressDTO address = DiagnosticPocDTO.AddressDTO.builder()
                                .addressId(0L)
                                .doorNo("")
                                .address1(h.getAddressLine1())
                                .address2("")
                                .cityName(h.getCity())
                                .stateName(h.getState())
                                .areaName(h.getLocality())
                                .pinCode(h.getPincode())
                                .locality(h.getLocality())
                                .build();

                        DiagnosticPocDTO.CdssOptionsDTO cdss = DiagnosticPocDTO.CdssOptionsDTO.builder()
                                .doctorEditable(true)
                                .doctorSpecific(false)
                                .brandSpecific(false)
                                .brandDefaults(true)
                                .build();

                        DiagnosticPocDTO.AgreementDTO agreement = DiagnosticPocDTO.AgreementDTO.builder()
                                .packageIdList(new ArrayList<>())
                                .build();

                        // Slugs look like: gvk-health-hub-jubilee-hills
                        String slug = (h.getName() + "-" + (h.getLocality() != null ? h.getLocality() : ""))
                                .toLowerCase()
                                .replaceAll("[^a-z0-9\\-]", "-")
                                .replaceAll("-+", "-")
                                .replaceAll("^-|-$", "");

                        return DiagnosticPocDTO.builder()
                                .pocId(h.getId())
                                .pocName(h.getName())
                                .address(address)
                                .locality(h.getLocality())
                                .areaName(h.getLocality())
                                .email("info@gvkhealthhub.com")
                                .contactList(h.getPhone() != null ? List.of(h.getPhone()) : List.of("040 27772888"))
                                .brandId(request.getBrandId() != null ? request.getBrandId() : 55L)
                                .brandName("GVK Health Hub")
                                .pdfHeaderType(1)
                                .payOnDeliveryAvailable(true)
                                .pharmacyHomeDeliveryAvailable(true)
                                .diagnosticSampleCollectionAvailable(true)
                                .productWalkinAvailable(true)
                                .productHomeDeliveryAvailable(true)
                                .localDiagnosticPartner(true)
                                .localPharmacyPartner(true)
                                .receptionistAvailable(true)
                                .pharmacyWalkinAvailable(true)
                                .diagnosticWalkinAvailable(true)
                                .centralPoc(true)
                                .cdssOptions(cdss)
                                .hasDigi(true)
                                .disablePOC(false)
                                .availableDaysList(new ArrayList<>())
                                .consultationFee(0.0)
                                .videoLaterConsultationFee(0.0)
                                .agreement(agreement)
                                .pocImageUrls(new ArrayList<>())
                                .pocType(request.getPocType() != null ? request.getPocType() : 1)
                                .discountText("Flat 10% Off")
                                .scanAndUploadPrescriptions(true)
                                .pocSlug(slug)
                                .serviceList(new ArrayList<>())
                                .isSelected(false)
                                .build();
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success("Diagnostic POCs retrieved successfully", dtos);
        } catch (Exception e) {
            log.error("[DiagnosticService] getDiagnosticPocs failed: {}", e.getMessage(), e);
            return ApiResponse.error("Failed to retrieve diagnostic POCs: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Mappers  (Entity → DTO)
    //  All field names match the REAL production API JSON exactly.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Maps DiagnosticCategory → DiagnosticCategoryDTO.
     * Real API response shows price fields as 0 for categories.
     */
    private DiagnosticCategoryDTO toCategoryDTO(DiagnosticCategory cat) {
        List<DiagnosticCategoryDTO> subDtos = new ArrayList<>();
        if (cat.getSubServiceList() != null) {
            subDtos = cat.getSubServiceList().stream()
                    .filter(sub -> Boolean.TRUE.equals(sub.getIsActive()))
                    .map(this::toCategoryDTO)
                    .collect(Collectors.toList());
        }

        return DiagnosticCategoryDTO.builder()
                // Real API: price fields present but all zero at category level
                .grossPrice(0.0)
                .discountPrice(0.0)
                .netPrice(0.0)
                .quantity(0)
                .originalAmount(0.0)
                .otherDiscountAmount(0.0)
                .taxationAmount(0.0)
                .finalAmount(0.0)
                .discountType(0)
                // Identity
                .categoryId(cat.getCategoryId())
                .categoryName(cat.getCategoryName())
                .imageUrl(cat.getImageUrl())
                // Always return a list (empty [] if no children) so FE forEach never crashes
                .subServiceList(subDtos)
                .build();
    }

    /**
     * Maps DiagnosticTest → DiagnosticTestDTO.
     *
     * All field names and structure verified against real production response:
     *   POST https://api-gvk.healthsignz.com/POZAppServices/investigationcontrol/tests
     *
     * Key notes from real response:
     *  - homeCollections is Integer (0 or 1), NOT boolean
     *  - homeOrderPriceDetails only present if homeCollections == 1
     *  - walkinOrderPriceDetails always present
     *  - quantity, originalAmount, otherDiscountAmount, taxationAmount, finalAmount
     *    all default to 0 in real API (set on order, not catalog)
     *  - vacutainerList is null in real response
     */
    private DiagnosticTestDTO toTestDTO(DiagnosticTest test) {
        // ---- Build sub-test list (for packages/profiles) ----
        List<DiagnosticTestDTO> subDtos = new ArrayList<>();
        if (test.getSubServiceList() != null) {
            subDtos = test.getSubServiceList().stream()
                    .filter(sub -> Boolean.TRUE.equals(sub.getIsActive()))
                    .map(this::toTestDTO)
                    .collect(Collectors.toList());
        }

        // ---- Build shared price values ----
        double grossPrice  = toDouble(test.getGrossPrice());
        double discountPrice = toDouble(test.getDiscountPrice());
        double netPrice    = toDouble(test.getNetPrice());

        // ---- walkinOrderPriceDetails — always present ----
        OrderPriceDetails walkin = OrderPriceDetails.builder()
                .grossPrice(grossPrice)
                .discountPrice(discountPrice)
                .taxes(null)
                .netPrice(netPrice)
                .type(0)
                .dayBasedPricing(null)
                .specialPriceList(null)
                .build();

        // ---- homeOrderPriceDetails — only when homeCollections == 1 ----
        OrderPriceDetails homeOrder = null;
        if (test.getHomeCollections() != null && test.getHomeCollections() == 1) {
            homeOrder = OrderPriceDetails.builder()
                    .grossPrice(grossPrice)
                    .discountPrice(discountPrice)
                    .taxes(null)
                    .netPrice(netPrice)
                    .type(0)
                    .dayBasedPricing(null)
                    .specialPriceList(null)
                    .build();
        }

        return DiagnosticTestDTO.builder()
                // ---- Top-level price fields (match real API) ----
                .grossPrice(grossPrice)
                .discountPrice(discountPrice)
                .netPrice(netPrice)
                .quantity(0)                     // always 0 in catalog (set on order)
                .originalAmount(0.0)             // always 0 in catalog
                .otherDiscountAmount(0.0)        // always 0 in catalog
                .taxationAmount(0.0)             // always 0 in catalog
                .finalAmount(0.0)                // always 0 in catalog
                .discountType(test.getDiscountType() != null ? test.getDiscountType() : 0)

                // ---- Service identity ----
                .serviceId(test.getServiceId())
                .serviceName(test.getServiceName())
                .slugName(test.getSlugName())
                .tags(test.getTags() != null ? test.getTags() : test.getServiceName())

                // ---- Parent category info ----
                .parentServiceId(test.getParentServiceId())
                .parentServiceName(test.getParentServiceName())
                .categoryId(test.getCategoryId())
                .categoryName(test.getCategoryName())

                // ---- Department (sub-category) ----
                .departmentId1(test.getDepartmentId1())
                .departmentId2(test.getDepartmentId2())
                .departmentName1(test.getDepartmentName1())
                .departmentName2(test.getDepartmentName2())

                // ---- Nested price details ----
                .homeOrderPriceDetails(homeOrder)           // null if walk-in only
                .walkinOrderPriceDetails(walkin)

                // ---- Logistics ----
                .pocId(test.getPocId())
                .scheduleId(test.getScheduleId())
                .scheduleType(test.getScheduleType())
                .cityId(test.getCityId() != null ? test.getCityId() : 0L)
                .doctorId(test.getDoctorId() != null ? test.getDoctorId() : 0L)
                .homeCollections(test.getHomeCollections())
                .expiryDate(test.getExpiryDate())

                // ---- Optional / SEO ----
                .precaution(test.getPrecaution())
                .metaTitle(test.getMetaTitle())
                .metaDescription(test.getMetaDescription())
                .vacutainerList(null)                       // always null in real API

                // ---- Sub-tests (packages/profiles) ----
                .subServiceList(subDtos.isEmpty() ? null : subDtos)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private double toDouble(BigDecimal bd) {
        return (bd != null) ? bd.doubleValue() : 0.0;
    }

    private <T> List<T> nullSafeList(List<T> list) {
        return (list != null) ? list : new ArrayList<>();
    }
}
