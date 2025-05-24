package com.fifa.analytics.RestModels;

import com.fifa.analytics.DTO.PlayingTime;
import lombok.Data;

@Data
public class PlayerStatisticsRest {
    private String playerId;
  private Integer seasonYear;
  private Integer scoredGoals;
  private PlayingTime playingTime;
}
