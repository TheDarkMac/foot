package com.fifa.analytics.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlayerStatistics {
    @JsonIgnore
    private Player player;
    private Integer scoredGoals;
    private PlayingTime playingTime;
    private Integer season;
}
