package com.fifa.app.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Coach {
    @JsonIgnore
    private String id;
    private String name;
    private Nationality nationality;

    public Coach(String coachName, String coachNationality) {
    }
}
