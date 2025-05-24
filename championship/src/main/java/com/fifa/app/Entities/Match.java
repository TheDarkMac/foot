package com.fifa.app.Entities;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Match {
    private String id;
    private Club clubPlayingHome;
    private Club clubPlayingAway;
    private String stadium;
    private LocalDateTime matchDatetime;
    private MatchStatus actualStatus;
    private int homeScore;
    private int awayScore;
    private List<Scorer> homeScorers;
    private List<Scorer> awayScorers;

    public Match(String matchId, Club home, Club away, String stadium, LocalDateTime datetime, MatchStatus status) {
        this.id = matchId;
        this.clubPlayingHome = home;
        this.clubPlayingAway = away;
        this.stadium = stadium;
        this.matchDatetime = datetime;
        this.actualStatus = status;
    }

}
