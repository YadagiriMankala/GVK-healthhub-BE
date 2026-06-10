package com.gvk.healthhub.repository;

import com.gvk.healthhub.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {

    Optional<Language> findByNameIgnoreCase(String name);

    @Query("SELECT COALESCE(MAX(l.id), 0) FROM Language l")
    Long findMaxId();
}
