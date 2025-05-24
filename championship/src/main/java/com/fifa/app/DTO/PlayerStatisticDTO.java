package com.fifa.app.DTO;

import com.fifa.app.Entities.PlayingTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PlayerStatisticDTO {
    private int scoredGoals;
    private PlayingTime playingTime;
}
