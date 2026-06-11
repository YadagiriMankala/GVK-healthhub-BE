package com.gvk.healthhub.controller;

import com.gvk.healthhub.dto.request.DiagnosticTestListRequest;
import com.gvk.healthhub.dto.request.DiagnosticPocRequest;
import com.gvk.healthhub.dto.response.ApiResponse;
import com.gvk.healthhub.dto.response.DiagnosticCategoryDTO;
import com.gvk.healthhub.dto.response.DiagnosticTestDTO;
import com.gvk.healthhub.dto.response.DiagnosticPocDTO;
import com.gvk.healthhub.service.DiagnosticService;
import com.gvk.healthhub.service.DiagnosticExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic Test Catalog Controller
 *
 * Mirrors the real POZAppServices/investigationcontrol/* endpoints:
 *
 *   GET  /POZAppServices/investigationcontrol/getdiagnosticscategory
 *        ?brandId=55&homeCollections=false&pinCode=500002&categoryId=0
 *
 *   POST /POZAppServices/investigationcontrol/tests
 *        Body: { pinCode, from, size, brandId, pocId, categoryId,
 *                departmentIdList1..4, patientProfileId, parentProfileId,
 *                isWellness, homeCollections, searchTerm, sortBy }
 *
 *   GET  /POZAppServices/investigationcontrol/packagetestdetails
 *        ?slugName=...&serviceId=0&pinCode=...&homeCollections=false
 *        &pocId=0&category=4&brandId=55
 *
 * Verified against:
 *   https://api-gvk.healthsignz.com/POZAppServices/investigationcontrol/*
 */
@RestController
@RequestMapping("/POZAppServices")
@Tag(name = "Diagnostic Test Catalog",
        description = "APIs for browsing diagnostic tests, packages, and categories")
public class DiagnosticController {

    private final DiagnosticService diagnosticService;
    private final DiagnosticExcelService diagnosticExcelService;

    @Autowired
    public DiagnosticController(DiagnosticService diagnosticService,
                                DiagnosticExcelService diagnosticExcelService) {
        this.diagnosticService = diagnosticService;
        this.diagnosticExcelService = diagnosticExcelService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1.  GET DIAGNOSTIC CATEGORIES
    //      Real call (GetTestCategories.ts):
    //        GET /investigationcontrol/getdiagnosticscategory
    //            ?brandId=55&homeCollections=false&categoryId=0&pinCode=500002
    //
    //      Real response: flat array of category objects, each with subServiceList
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/investigationcontrol/getdiagnosticscategory")
    @Operation(
            summary = "Get diagnostic categories",
            description = "Returns top-level diagnostic categories with nested subServiceList. " +
                    "Real endpoint: GET /POZAppServices/investigationcontrol/getdiagnosticscategory" +
                    "?brandId=55&homeCollections=false&categoryId=0&pinCode=500002"
    )
    public ResponseEntity<List<DiagnosticCategoryDTO>> getDiagnosticsCategory(
            @Parameter(description = "Brand ID, e.g. 55 for GVK")
            @RequestParam(required = false) Long brandId,

            @Parameter(description = "false=all, true=home collection tests only")
            @RequestParam(required = false) Boolean homeCollections,

            @Parameter(description = "Pin code of patient location")
            @RequestParam(required = false) String pinCode,

            @Parameter(description = "Root category filter (0 = all)")
            @RequestParam(required = false, defaultValue = "0") Long categoryId
    ) {
        ApiResponse<List<DiagnosticCategoryDTO>> response =
                diagnosticService.getDiagnosticsCategories(brandId, homeCollections, pinCode, categoryId);

        if (response.isSuccess() && response.getData() != null) {
            return ResponseEntity.ok(response.getData());
        }
        return ResponseEntity.ok(List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2.  POST TESTS LIST
    //      Real call (GetTests.ts):
    //        POST /investigationcontrol/tests
    //        Body: DiagnosticTestListRequest
    //
    //      Real response: flat JSON array (NOT paginated wrapper in prod)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/investigationcontrol/tests")
    @Operation(
            summary = "Get diagnostic tests list",
            description = "Returns paginated diagnostic tests/packages matching filters. " +
                    "Real endpoint: POST /POZAppServices/investigationcontrol/tests. " +
                    "Body: { pinCode, from, size, brandId, pocId, categoryId, " +
                    "departmentIdList1..4, homeCollections, searchTerm, sortBy }"
    )
    public ResponseEntity<List<DiagnosticTestDTO>> getTests(
            @RequestBody DiagnosticTestListRequest request) {

        ApiResponse<List<DiagnosticTestDTO>> response = diagnosticService.getTests(request);

        if (response.isSuccess() && response.getData() != null) {
            return ResponseEntity.ok(response.getData());
        }
        return ResponseEntity.ok(List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3.  GET PACKAGE / TEST DETAILS
    //      Real call (GetTestDetails.ts):
    //        GET /investigationcontrol/packagetestdetails
    //            ?slugName=complete-blood-picture-cbp&serviceId=0
    //            &pinCode=500002&homeCollections=false
    //            &pocId=0&category=4&brandId=55
    //
    //      Real response: single test object (same structure as tests list item)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/investigationcontrol/packagetestdetails")
    @Operation(
            summary = "Get diagnostic test / package details",
            description = "Returns full details of a single test or package. " +
                    "Looks up by slugName first, then serviceId as fallback. " +
                    "Real endpoint: GET /POZAppServices/investigationcontrol/packagetestdetails"
    )
    public ResponseEntity<?> getPackageTestDetails(
            @Parameter(description = "Slug name of the test/package (primary key)")
            @RequestParam(required = false, defaultValue = "") String slugName,

            @Parameter(description = "Service ID (fallback if slugName empty)")
            @RequestParam(required = false) Long serviceId,

            @Parameter(description = "Pin code")
            @RequestParam(required = false) String pinCode,

            @Parameter(description = "false=walk-in, true=home collection")
            @RequestParam(required = false) Boolean homeCollections,

            @Parameter(description = "POC / hospital ID")
            @RequestParam(required = false) Long pocId,

            @Parameter(description = "Category type (1-5)")
            @RequestParam(required = false) Integer category,

            @Parameter(description = "Brand ID, e.g. 55")
            @RequestParam(required = false) Long brandId
    ) {
        ApiResponse<DiagnosticTestDTO> response =
                diagnosticService.getTestDetails(slugName, serviceId);

        if (response.getStatusCode() != null && response.getStatusCode() == 404) {
            return ResponseEntity.notFound().build();
        }
        // Real API returns the test object directly (not wrapped)
        return ResponseEntity.ok(response.getData());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  4.  PATCH UPDATE TEST
    //      Endpoint: PATCH /POZAppServices/investigationcontrol/tests/{serviceId}
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping("/investigationcontrol/tests/{serviceId}")
    @Operation(
            summary = "Update a diagnostic test / package",
            description = "Applies a partial update (PATCH) to the diagnostic test or package identified by serviceId."
    )
    public ResponseEntity<?> updateTest(
            @PathVariable Long serviceId,
            @RequestBody com.gvk.healthhub.dto.request.DiagnosticTestUpdateRequest request
    ) {
        ApiResponse<DiagnosticTestDTO> response = diagnosticService.updateTest(serviceId, request);
        if (response.getStatusCode() != null && response.getStatusCode() == 404) {
            return ResponseEntity.notFound().build();
        }
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response.getMessage());
        }
        return ResponseEntity.ok(response.getData());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  5.  EXPORT DIAGNOSTIC TESTS
    //      Endpoint: GET /POZAppServices/investigationcontrol/export
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/investigationcontrol/export")
    @Operation(
            summary = "Export diagnostic tests to Excel",
            description = "Download an Excel spreadsheet of all diagnostic tests and packages with drop-down data validation"
    )
    public ResponseEntity<InputStreamResource> exportTests() {
        ByteArrayInputStream in = diagnosticExcelService.exportTestsToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=diagnostic_tests.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  6.  IMPORT DIAGNOSTIC TESTS
    //      Endpoint: POST /POZAppServices/investigationcontrol/import
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping(value = "/investigationcontrol/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Import diagnostic tests from Excel",
            description = "Upload an Excel spreadsheet to bulk create or update diagnostic tests and packages"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> importTests(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> report = diagnosticExcelService.importTestsFromExcel(file.getInputStream());
            return ResponseEntity.ok(ApiResponse.success("Excel import processed successfully", report));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to process Excel import: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  7.  POST DIAGNOSTIC POC LIST
    //      Endpoint: POST /POZAppServices/consumercontroller/diagnosticpoc
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/consumercontroller/diagnosticpoc")
    @Operation(
            summary = "Get diagnostic POCs (Hospitals/Centers)",
            description = "Returns a list of diagnostic POCs matching filters such as pincode, homeCollections etc."
    )
    public ResponseEntity<List<DiagnosticPocDTO>> getDiagnosticPocs(
            @RequestBody DiagnosticPocRequest request
    ) {
        ApiResponse<List<DiagnosticPocDTO>> response = diagnosticService.getDiagnosticPocs(request);
        if (response.isSuccess() && response.getData() != null) {
            return ResponseEntity.ok(response.getData());
        }
        return ResponseEntity.ok(List.of());
    }
}
