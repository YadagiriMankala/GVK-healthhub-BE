package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    @Query("""
            SELECT s FROM DoctorSlot s
            WHERE s.doctorId = :doctorId
            AND s.hospitalId = :hospitalId
            AND s.serviceId = :serviceId
            AND s.slotTime >= :from
            AND s.vacantSlots > 0
            AND s.status = 1
            ORDER BY s.slotTime ASC
            """)
    List<DoctorSlot> findAvailableSlots(
            @Param("doctorId") Long doctorId,
            @Param("hospitalId") Long hospitalId,
            @Param("serviceId") Long serviceId,
            @Param("from") LocalDateTime from);
}
