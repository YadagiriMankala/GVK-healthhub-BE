package com.gvk.healthhub.mapper;

import com.gvk.healthhub.dto.request.DoctorUpdateRequest;
import com.gvk.healthhub.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Doctor update operations.
 * Uses NullValuePropertyMappingStrategy.IGNORE to support partial updates (Patch semantics).
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoctorUpdateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "doctorLanguages", ignore = true)
    @Mapping(target = "doctorSpecializations", ignore = true)
    @Mapping(target = "doctorHospitals", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    void updateDoctorFromDto(DoctorUpdateRequest dto, @MappingTarget Doctor entity);
}
