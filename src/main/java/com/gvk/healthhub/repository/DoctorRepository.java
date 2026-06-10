package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findBySlug(String slug);

    List<Doctor> findByIsActiveTrueOrderByAvgRatingDesc();

    Page<Doctor> findByIsActiveTrue(Pageable pageable);

    @Query("""
    		SELECT DISTINCT d
    		FROM Doctor d
    		LEFT JOIN DoctorSpecialization ds ON ds.doctor.id = d.id
    		LEFT JOIN DoctorHospital dh ON dh.doctor.id = d.id
    		WHERE d.isActive = true
    		AND (:serviceId IS NULL OR ds.specialization.id = :serviceId)
    		AND (:serviceIdsEmpty = true OR ds.specialization.id IN :serviceIdList)
    		AND (:pocIdsEmpty = true OR dh.hospital.id IN :pocIdList)
    		AND (:minExperience IS NULL OR d.experienceYears >= :minExperience)
    		AND (:maxExperience IS NULL OR d.experienceYears <= :maxExperience)
    		""")
    Page<Doctor> searchDoctors(
            @Param("serviceId") Long serviceId,
            @Param("serviceIdList") List<Long> serviceIdList,
            @Param("serviceIdsEmpty") boolean serviceIdsEmpty,
            @Param("pocIdList") List<Long> pocIdList,
            @Param("pocIdsEmpty") boolean pocIdsEmpty,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience,
            Pageable pageable);
    
    
    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND d.isFeatured = true ORDER BY d.avgRating DESC")
    List<Doctor> findFeaturedDoctors();

    boolean existsBySlug(String slug);
}