package com.fifa.app.Mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import com.fifa.app.DTO.PlayerDTO;
import com.fifa.app.Entities.*;

public class PlayerMapper {

    public static Player mapPlayerFromResultSet(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getObject("player_id").toString());
        player.setName(rs.getString("name"));
        player.setNumber(rs.getInt("number"));
        player.setPosition(Position.valueOf(rs.getString("player_position")));
        player.setNationality(Nationality.valueOf(rs.getString("nationality")));
        player.setAge(rs.getInt("age"));


        String clubId = rs.getString("club_id");
        if (clubId != null) {
            Club club = new Club();
            club.setId(clubId);
            club.setName(rs.getString("club_name"));
            club.setAcronym(rs.getString("acronym"));
            club.setYearCreation(rs.getInt("year_creation"));
            club.setStadium(rs.getString("stadium"));

            String coachId = rs.getString("coach_id");
            if (coachId != null) {
                Coach coach = new Coach();
                coach.setId(coachId);
                coach.setName(rs.getString("coach_name"));
                coach.setNationality(Nationality.valueOf(rs.getString("coach_nationality")));
                club.setCoach(coach);
            }

            player.setClub(club);
        } else {
            player.setClub(null);
        }

        return player;
    }

    public static PlayerDTO mapPlayerDtoFromResultSet(ResultSet rs) throws SQLException {
        PlayerDTO player = new PlayerDTO();
        player.setId(rs.getObject("player_id").toString());
        player.setName(rs.getString("name"));
        player.setNumber(rs.getInt("number"));
        player.setPosition(Position.valueOf(rs.getString("player_position")));
        player.setNationality(Nationality.valueOf(rs.getString("nationality")));
        player.setAge(rs.getInt("age"));
        return player;
    }
}
