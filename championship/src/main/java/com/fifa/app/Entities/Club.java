package com.fifa.app.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Club {
    private String id;
    private String name;
    private String acronym;
    private int yearCreation;
    private String stadium;
    private Coach coach;

    public Club(String id, String name, String acronym) {
        this.id = id;
        this.name = name;
        this.acronym = acronym;
    }

}
