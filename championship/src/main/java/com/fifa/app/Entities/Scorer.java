package com.fifa.app.Entities;

import com.fifa.app.DTO.PlayerScorerDTO;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Scorer {
    private PlayerScorerDTO player;
    private int minuteOfGoal;
    private boolean ownGoal;
}
