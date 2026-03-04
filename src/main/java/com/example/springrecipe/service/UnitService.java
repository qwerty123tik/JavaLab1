package com.example.springrecipe.service;

import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.repository.UnitRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;
    private final RecipeMapper mapper;

    public List<UnitDTO> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(mapper::toUnitDto)
                .toList();
    }

    @Transactional
    public UnitDTO createUnit(UnitDTO dto) {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setName(dto.getName());
        unit.setAbbreviation(dto.getAbbreviation());

        unit = unitRepository.save(unit);
        return mapper.toUnitDto(unit);
    }
}
