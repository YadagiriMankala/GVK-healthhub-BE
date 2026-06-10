package com.gvk.healthhub.entity;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_doctor_hospital")
public class DoctorHospital {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Doctor doctor;

	@ManyToOne
	private Hospital hospital;

	private BigDecimal walkinFee;

	private BigDecimal videoFee;
}