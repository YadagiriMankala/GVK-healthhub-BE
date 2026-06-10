package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByNameIgnoreCase(String name);

    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Specialization s")
    Long findMaxId();
}
