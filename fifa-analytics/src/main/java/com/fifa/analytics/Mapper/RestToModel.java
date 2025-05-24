package com.fifa.analytics.Mapper;

import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.ClubStat;
import com.fifa.analytics.DTO.Player;
import com.fifa.analytics.DTO.PlayerStatistics;
import com.fifa.analytics.Enum.Championship;
import com.fifa.analytics.RestModels.ClubRest;
import com.fifa.analytics.RestModels.PlayerRest;
import com.fifa.analytics.RestModels.PlayerStatisticsRest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestToModel {
    public static Player mapToPlayer(PlayerRest rest){
        Player player = new Player();
        player.setId(rest.getId());
        player.setName(rest.getName());
        player.setNumber(rest.getNumber());
        player.setPosition(rest.getPosition());
        player.setAge(rest.getAge());
        player.setClub(rest.getClub());
        player.setNationality(rest.getNationality());
        return player;
    }

    public static Club mapToClub(ClubRest clubRest){
        Club club = new Club();
        club.setId(clubRest.getId());
        club.setName(clubRest.getName());
        club.setYearCreation(clubRest.getYearCreation());
        club.setStadium(clubRest.getStadium());
        club.setCoach(clubRest.getCoach());
        club.setAcronym(clubRest.getAcronym());
        club.setCoach(clubRest.getCoach());
        club.setChampionship(Championship.valueOf(clubRest.getChampionshipName()));
        ClubStat clubStat = new ClubStat();
        clubStat.setRankingPoints(clubRest.getRankingPoints());
        clubStat.setDifferenceGoals(clubRest.getDifferenceGoals());
        clubStat.setScoredGoals(clubRest.getScoredGoals());
        clubStat.setConcededGoals(clubRest.getConcededGoals());
        clubStat.setCleanSheetNumber(clubRest.getCleanSheetNumber());
        clubStat.setClub(club);
        club.setClubStats(List.of(clubStat));
        club.setCoach(clubRest.getCoach());
        return club;
    }

    public static ClubRest mapToClubRest(Club club){
        ClubRest clubRest = new ClubRest();
        clubRest.setId(club.getId());
        clubRest.setName(club.getName());
        clubRest.setAcronym(club.getAcronym());
        clubRest.setYearCreation(club.getYearCreation());
        clubRest.setStadium(club.getStadium());
        clubRest.setCoach(club.getCoach());
        clubRest.setRankingPoints(club.getClubStats().getFirst().getRankingPoints());
        clubRest.setScoredGoals(club.getClubStats().getFirst().getScoredGoals());
        clubRest.setConcededGoals(club.getClubStats().getFirst().getConcededGoals());
        clubRest.setDifferenceGoals(club.getClubStats().getFirst().getDifferenceGoals());
        clubRest.setCleanSheetNumber(club.getClubStats().getFirst().getCleanSheetNumber());
        clubRest.setChampionshipName(club.getChampionship().name());
        return clubRest;
    }

    public static PlayerStatistics mapToPlayerStatistics(PlayerStatisticsRest playerStatisticsRest) {
        PlayerStatistics playerStatistics = new PlayerStatistics();
        Player player = new Player();
        player.setId(playerStatisticsRest.getPlayerId());
        playerStatistics.setPlayer(player);
        playerStatistics.setScoredGoals(playerStatisticsRest.getScoredGoals());
        playerStatistics.setPlayingTime(playerStatisticsRest.getPlayingTime());
        playerStatistics.setSeason(playerStatisticsRest.getSeasonYear());
        return playerStatistics;
    }
}
