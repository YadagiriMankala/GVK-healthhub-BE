package com.gvk.healthhub.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DiagnosticPocRequest {
    private Long parentProfileId;
    private Boolean homeCollections;
    private Integer pocType;
    private String pinCode;
    private Double latitude;
    private Double longitude;
    private Long brandId;
    private Integer from;
    private Integer size;
    private List<Long> serviceIdList;
}
