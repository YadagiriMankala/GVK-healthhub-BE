package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for:
 *   GET /POZAppServices/investigationcontrol/getdiagnosticscategory
 *       ?brandId=55&homeCollections=false&pinCode=500002&categoryId=0
 *
 * Field names exactly match the REAL production API JSON response.
 * Verified against: https://api-gvk.healthsignz.com/POZAppServices/investigationcontrol/getdiagnosticscategory
 *
 * The real API returns each category with the same price wrapper fields
 * (all zeros at category level) plus a subServiceList of child categories.
 * The FE TestController.tsx reads categoryId from each item's subServiceList.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosticCategoryDTO {

    // ---- Pricing wrapper (always 0 for categories; real prices on tests) ----
    private Double grossPrice;
    private Double discountPrice;
    private Double netPrice;
    private Integer quantity;
    private Double originalAmount;
    private Double otherDiscountAmount;
    private Double taxationAmount;
    private Double finalAmount;
    private Integer discountType;

    // ---- Category identity ----
    private Long categoryId;
    private String categoryName;

    /** imageUrl shown on the category tile (custom field, not in prod API) */
    private String imageUrl;

    /**
     * Sub-categories (child categories).
     * Real API: each top-level category contains subServiceList of sub-categories.
     * FE TestController.tsx iterates this to build filter chips.
     */
    private List<DiagnosticCategoryDTO> subServiceList;
}
