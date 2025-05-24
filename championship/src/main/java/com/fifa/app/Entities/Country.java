package com.fifa.app.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Country {
    private String id; // ex : madagascar -> MDG , france -> FR ( amzay tsy mila mitadidy be )
    private String name;
}
