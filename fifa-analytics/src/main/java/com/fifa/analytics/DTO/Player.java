package com.fifa.analytics.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fifa.analytics.Enum.Position;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Player{
    private String id;
    private String name;
    private Integer number;
    private Position position;
    private String nationality;
    private Integer age;
    @JsonIgnore
    private Club club;
    private List<PlayerStatistics> playerStatistics;
}
