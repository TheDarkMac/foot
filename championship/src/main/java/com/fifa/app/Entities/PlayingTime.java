package com.fifa.app.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PlayingTime {
    private long value;
    private DurationUnit durationUnit;
}
