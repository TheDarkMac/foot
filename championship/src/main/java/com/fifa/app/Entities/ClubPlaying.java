package com.fifa.app.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClubPlaying {
    private String id;
    private String name;
    private String acronym;
    private int score;
    private List<Scorer> scorers;
}
