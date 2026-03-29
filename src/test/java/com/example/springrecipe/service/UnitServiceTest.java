package com.example.springrecipe.service;

import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.exceptions.UnitNotFoundException;
import com.example.springrecipe.mapper.RecipeMapper;
import com.example.springrecipe.model.UnitOfMeasure;
import com.example.springrecipe.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private RecipeMapper mapper;

    @InjectMocks
    private UnitService unitService;

    private UnitDTO dto() {
        UnitDTO dto = new UnitDTO();
        dto.setName("грамм");
        dto.setAbbreviation("г");
        return dto;
    }

    @Test
    void getAllUnits_success() {
        UnitOfMeasure unit = new UnitOfMeasure();
        unit.setName("грамм");

        when(unitRepository.findAll()).thenReturn(List.of(unit));
        when(mapper.toUnitDto(any())).thenReturn(dto());

        List<UnitDTO> result = unitService.getAllUnits();

        assertEquals(1, result.size());
        verify(unitRepository).findAll();
    }

    @Test
    void createUnit_success() {
        UnitDTO dto = dto();

        when(unitRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(mapper.toUnitDto(any())).thenReturn(dto);

        UnitDTO result = unitService.createUnit(dto);

        assertEquals("грамм", result.getName());
        assertEquals("г", result.getAbbreviation());
        verify(unitRepository).save(any());
    }

    @Test
    void deleteUnit_success() {
        when(unitRepository.existsById(1L)).thenReturn(true);

        unitService.deleteUnit(1L);

        verify(unitRepository).deleteById(1L);
    }

    @Test
    void deleteUnit_notFound() {
        when(unitRepository.existsById(1L)).thenReturn(false);

        assertThrows(UnitNotFoundException.class, () -> unitService.deleteUnit(1L));

        verify(unitRepository, never()).deleteById(any());
    }
}
