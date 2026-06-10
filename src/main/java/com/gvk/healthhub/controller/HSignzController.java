package com.gvk.healthhub.controller;

import com.gvk.healthhub.dto.response.ApiResponse;
import com.gvk.healthhub.entity.Specialization;
import com.gvk.healthhub.repository.SpecializationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/HSignzAppServices")
public class HSignzController {

    private final SpecializationRepository specializationRepository;

    @Autowired
    public HSignzController(SpecializationRepository specializationRepository) {
        this.specializationRepository = specializationRepository;
    }

    @GetMapping("/mCommerce/servicecategory")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceCategory(
            @RequestParam(required = false) String pinCode,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Boolean videoDoctorsOnly,
            @RequestParam(required = false) Long pocId) {

        List<Specialization> specializations = specializationRepository.findAll();

        // Map database specializations to frontend sub_categories
        List<Map<String, Object>> specSubCategories = specializations.stream()
                .map(spec -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", spec.getId());
                    item.put("serviceId", spec.getId());
                    item.put("serviceName", spec.getName());
                    item.put("name", spec.getName());
                    item.put("priority", 1);
                    item.put("categoryId", 3);
                    item.put("sub_categories", new ArrayList<>());
                    return item;
                })
                .collect(Collectors.toList());

        // Category with categoryId = 3 (Consultations)
        Map<String, Object> consultationsCategory = new HashMap<>();
        consultationsCategory.put("categoryId", 3);
        consultationsCategory.put("categoryName", "Consultations");
        consultationsCategory.put("sub_categories", specSubCategories);

        // Root category
        Map<String, Object> rootCategory = new HashMap<>();
        rootCategory.put("categoryId", 0);
        rootCategory.put("categoryName", "Root");
        rootCategory.put("sub_categories", List.of(consultationsCategory));

        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", rootCategory));
    }
}
