package com.gvk.healthhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "t_doctor_specialization")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialization {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Doctor doctor;

    @ManyToOne
    private Specialization specialization;
}