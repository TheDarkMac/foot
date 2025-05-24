package com.example.demo.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@AllArgsConstructor
@Getter
@Setter
public class PlayerStat {
    private Long id;
    private Long playerId;
    private Long redCard;
    private Long yellowCard;
    private Long goal;
    private Long pass;
    private Long decisivePass;
    private Long blockedGoal;
    private Duration gameTime;
}
