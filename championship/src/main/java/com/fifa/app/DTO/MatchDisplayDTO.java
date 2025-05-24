package com.fifa.app.DTO;

import com.fifa.app.Entities.ClubPlaying;
import com.fifa.app.Entities.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MatchDisplayDTO {
    private String id;
    private ClubPlaying clubPlayingHome;
    private ClubPlaying clubPlayingAway;
    private String stadium;
    private LocalDate matchDatetime;
    private MatchStatus actualStatus;
}
