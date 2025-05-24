package com.fifa.app.Mapper;

import com.fifa.app.DTO.PlayerStatisticDTO;
import com.fifa.app.Entities.DurationUnit;
import com.fifa.app.Entities.PlayingTime;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerStatisticMapper {

    public static PlayerStatisticDTO mapFromResultSet(ResultSet rs) throws SQLException {
        int scoredGoals = rs.getInt("scored_goal");
        long playingTimeInSeconds = rs.getLong("playing_time");

        PlayingTime playingTime = new PlayingTime(
            playingTimeInSeconds,
            DurationUnit.SECONDS
        );

        return new PlayerStatisticDTO(scoredGoals, playingTime);
    }
}
