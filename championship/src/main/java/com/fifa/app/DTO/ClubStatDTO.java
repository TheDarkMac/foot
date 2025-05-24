package com.fifa.app.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClubStatDTO {
    private int draw;
    private int playedMatch;
    private int win;
    private int loss;
    private int takenGoal;
    private int scoredGoal;
}
