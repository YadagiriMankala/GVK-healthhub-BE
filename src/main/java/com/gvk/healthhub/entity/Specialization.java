package com.gvk.healthhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "t_specialization")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specialization {
	@Id
	private Long id;

	private String name;
}

