package com.example.springrecipe.controller;

import com.example.springrecipe.dto.UnitDTO;
import com.example.springrecipe.service.UnitService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/units")
@AllArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<List<UnitDTO>> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @PostMapping
    public ResponseEntity<UnitDTO> createUnit(@RequestBody UnitDTO dto) {
        return new ResponseEntity<>(unitService.createUnit(dto), HttpStatus.CREATED);
    }
}
