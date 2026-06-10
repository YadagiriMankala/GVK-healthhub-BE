package com.gvk.healthhub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for doctor list API - matches existing frontend request format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DoctorListRequest {

    private String serviceName;
    private Long serviceId;
    private Long profileId;
    private Boolean participateVideoLater;
    private Boolean participatePhysically;
    private List<Long> pocIdList;
    private List<Long> serviceIdList;
    private Long brandId;
    private Long pocId;
    private Double distance;
    private List<Long> languageIdList;
    private Integer minExperience;
    private Integer maxExperience;
    private Boolean timeConsidered;
    private Long categoryId;
    private BigDecimal originalPrice;
    private Boolean sortPriceAsc;
    private Boolean sortPriceDesc;
    private Boolean sortExperienceAsc;
    private Boolean sortExperienceDesc;
    private Double latitude;
    private Double longitude;
    private String pinCode;
    private Long packageId;
    private Integer from;
    private Integer size;
}