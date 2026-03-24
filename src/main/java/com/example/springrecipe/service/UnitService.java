package com.example.springrecipe.service;

import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.repository.UnitRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;
    private final RecipeMapper mapper;

    public List<UnitDTO> getAllUnits() {
        log.debug("Запрос всех единиц измерения");
        List<UnitDTO> result = unitRepository.findAll().stream()
                .map(mapper::toUnitDto)
                .toList();
        log.info("Найдено {} единиц измерения", result.size());
        return result;
    }

    @Transactional
    public UnitDTO createUnit(UnitDTO dto) {
        log.info("Создание единицы измерения: название='{}', аббревиатура='{}'",
                dto.getName(), dto.getAbbreviation());

        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setName(dto.getName());
        unit.setAbbreviation(dto.getAbbreviation());

        unit = unitRepository.save(unit);
        log.info("Единица измерения успешно создана: ID={}, название='{}'", unit.getId(), unit.getName());
        return mapper.toUnitDto(unit);
    }

    @Transactional
    public void deleteUnit(Long id) {
        log.warn("УДАЛЕНИЕ ЕДИНИЦЫ ИЗМЕРЕНИЯ: ID={}", id);

        if (!unitRepository.existsById(id)) {
            log.warn("Единица измерения с ID {} не найдена для удаления", id);
            throw new UnitNotFoundException("Unit not found");
        }

        unitRepository.deleteById(id);
        log.info("Единица измерения ID={} успешно удалена", id);
    }
}
