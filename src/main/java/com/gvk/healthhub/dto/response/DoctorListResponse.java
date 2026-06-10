package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response DTO for doctor list API - matches existing frontend response format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorListResponse {

    private List<DoctorDTO> doctors;
    private int totalCount;
    private boolean hasMore;

    // Getters and Setters
    public List<DoctorDTO> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<DoctorDTO> doctors) {
        this.doctors = doctors;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}