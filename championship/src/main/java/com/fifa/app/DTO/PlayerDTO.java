package com.fifa.app.DTO;

import com.fifa.app.Entities.Club;
import com.fifa.app.Entities.Country;
import lombok.AllArgsConstructor;
import com.fifa.app.Entities.Nationality;
import com.fifa.app.Entities.Position;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlayerDTO {
    private String id;
    private String name;
    private int age;
    private int number;
    private Position position;
    private Nationality nationality;
}
