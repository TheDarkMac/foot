package com.fifa.analytics.RestModels;

import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.Enum.Position;
import lombok.Data;

@Data
public class PlayerRest {
    private String id;
    private String name;
    private Integer number;
    private Position position;
    private String nationality;
    private Integer age;
    private Club club;
}
