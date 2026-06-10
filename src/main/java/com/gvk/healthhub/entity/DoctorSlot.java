package com.gvk.healthhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_doctor_slot")
@Data
public class DoctorSlot {

	@Id
    @GeneratedValue
    private Long id;

    private Long doctorId;

    private Long hospitalId;

    private Long serviceId;

    private LocalDateTime slotTime;

    private Integer vacantSlots;

    private BigDecimal slotPrice;

    private Integer status;
}
