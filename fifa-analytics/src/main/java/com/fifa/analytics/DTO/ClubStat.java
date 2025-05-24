package com.fifa.analytics.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ClubStat {
    @JsonIgnore
    private Club club;
    @JsonIgnore
    private Season season;
    private Integer rankingPoints;
    private Integer scoredGoals;
    private Integer concededGoals;
    private Integer differenceGoals;
    private Integer cleanSheetNumber;
}
