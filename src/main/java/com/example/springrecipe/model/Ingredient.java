package com.example.springrecipe.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingredients")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
   // private Double quantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private UnitOfMeasure unit;

    @ManyToMany(mappedBy = "ingredients")
    private List<Recipe> recipes = new ArrayList<>();
}
