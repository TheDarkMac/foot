package com.fifa.analytics.DTO;

import com.fifa.analytics.Enum.DurationUnit;
import lombok.Data;

@Data
public class PlayingTime {
    private Double value;
    private DurationUnit durationUnit;
}
