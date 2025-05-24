package com.fifa.app.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClubStat {
    private int draw;
    private int playedMatch;
    private int win;
    private int loss;
    private int takenGoal;
    private int scoredGoal;
}
