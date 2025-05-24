package com.fifa.app.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private String id;
    private String name;
    private int age;
    private Club club;
    private int number;
    private Position position;
    private Nationality nationality;


}
