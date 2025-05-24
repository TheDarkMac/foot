package com.fifa.analytics.RestModels;

import com.fifa.analytics.DTO.Coach;
import lombok.Data;

@Data
public class ClubRest {
    private String id;
    private String name;
    private String acronym;
    private Integer yearCreation;
    private String stadium;
    private Coach coach;
    private Integer rankingPoints;
    private Integer scoredGoals;
    private Integer concededGoals;
    private Integer differenceGoals;
    private Integer cleanSheetNumber;
    private String championshipName;
    private Integer season;
}
