package com.fifa.analytics.DAO;

import com.fifa.analytics.DTO.PlayerStatistics;
import com.fifa.analytics.DTO.PlayingTime;
import com.fifa.analytics.DTO.Season;
import com.fifa.analytics.Enum.DurationUnit;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Repository
public class PlayerStatisticsDAO {
    private DataConnection dataConnection;
    private SeasonDAO seasonDAO;

    public List<PlayerStatistics> getPlayerStatistics(String playerId) {
        List<PlayerStatistics> playerStatistics = new ArrayList<>();
        String query =  "SELECT player_id,scored_goals,playing_time,playing_time_unit,season_year FROM player_statistics WHERE player_id = ?::UUID";
        try(Connection conn = dataConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                PlayerStatistics playerStatistic = mapFromResultSet(rs);
                playerStatistics.add(playerStatistic);
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return playerStatistics;
    }

    public List<PlayerStatistics> saveAll(List<PlayerStatistics> playerStatistics) {
        List<PlayerStatistics> playerStatisticsList = new ArrayList<>();
        String query = "INSERT INTO player_statistics (player_id,scored_goals,playing_time,playing_time_unit,season_year) VALUES (?,?,?,?,?)" +
                "ON CONFLICT (player_id,season_year) DO UPDATE SET " +
                "scored_goals = EXCLUDED.scored_goals," +
                "playing_time = EXCLUDED.playing_time," +
                "playing_time_unit = EXCLUDED.playing_time_unit " +
                "RETURNING player_id,scored_goals,playing_time,playing_time_unit,season_year";
        playerStatistics.forEach(player -> {
            try (Connection connection = dataConnection.getConnection()){
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setObject(1,player.getPlayer().getId(),Types.OTHER);
                ps.setDouble(2,player.getScoredGoals());
                ps.setDouble(3,player.getPlayingTime().getValue());
                ps.setObject(4,player.getPlayingTime().getDurationUnit(), Types.OTHER);
                ps.setInt(5,player.getSeason());
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    PlayerStatistics playerStatistic = mapFromResultSet(rs);
                    playerStatisticsList.add(playerStatistic);
                }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return playerStatisticsList;
    }

    public PlayerStatistics mapFromResultSet(ResultSet resultSet) throws SQLException {
        Season season = seasonDAO.getSeason(resultSet.getInt("season_year"));
        PlayingTime playingTime = new PlayingTime();
        playingTime.setValue(resultSet.getDouble("playing_time"));
        playingTime.setDurationUnit(DurationUnit.valueOf(resultSet.getString("playing_time_unit")));
        PlayerStatistics playerStatistics = new PlayerStatistics();
        playerStatistics.setScoredGoals(resultSet.getInt("scored_goals"));
        playerStatistics.setPlayingTime(playingTime);
        playerStatistics.setSeason(season.getYear());
        return playerStatistics;
    }
}
