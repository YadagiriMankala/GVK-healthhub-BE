package com.gvk.healthhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DiagnosticCategory entity - represents a diagnostic test category
 * (e.g. Blood Tests, Radiology, Health Packages, etc.)
 * Mirrors the FE model: DiagnosticServiceCategory.ts
 *
 * API: GET /POZAppServices/investigationcontrol/getdiagnosticscategory
 *       ?brandId=55&homeCollections=false&pinCode=500033&categoryId=1001
 */
@Entity
@Table(name = "t_diagnostic_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 255, nullable = false)
    private String categoryName;

    /**
     * Parent category – null means this is a top-level (parent) category.
     * Self-referential FK mirrors subServiceList in the FE type.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private DiagnosticCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<DiagnosticCategory> subServiceList = new ArrayList<>();

    /** Brand this category belongs to (matches brandId query param) */
    @Column(name = "brand_id")
    private Long brandId;

    /** Whether category is available for home collections */
    @Column(name = "home_collections")
    private Boolean homeCollections;

    /** Optional pin-code restriction (null = available everywhere) */
    @Column(name = "pin_code", length = 20)
    private String pinCode;

    /** Display order on the UI */
    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
