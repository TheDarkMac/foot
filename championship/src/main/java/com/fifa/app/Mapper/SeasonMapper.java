package com.fifa.app.Mapper;

import com.fifa.app.Entities.Season;
import com.fifa.app.Entities.SeasonStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeasonMapper {

    public static Season mapFromResultSet(ResultSet rs) throws SQLException {
        return new Season(
            rs.getObject("id").toString(),
            rs.getInt("year"),
            rs.getString("alias"),
            SeasonStatus.valueOf(rs.getString("status"))
        );
    }

    public static boolean isValidStatusTransition(SeasonStatus current, SeasonStatus next) {
        return switch (current) {
            case NOT_STARTED -> next == SeasonStatus.STARTED;
            case STARTED -> next == SeasonStatus.FINISHED;
            case FINISHED -> false;
        };
    }
}
