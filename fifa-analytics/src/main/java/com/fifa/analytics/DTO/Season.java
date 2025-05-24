package com.fifa.analytics.DTO;

import com.fifa.analytics.Enum.Status;
import lombok.Data;

@Data
public class Season {
    private String id;
    private Integer year;
    private String alias;
    private Status status;
}
