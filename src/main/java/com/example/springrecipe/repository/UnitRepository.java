package com.example.springrecipe.repository;

import com.example.springrecipe.model.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<UnitOfMeasure, Long> {
    Optional<UnitOfMeasure> findByAbbreviation(String abbreviation);
}
