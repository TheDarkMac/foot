package com.fifa.app.DAO;

import com.fifa.app.DTO.PlayerStatisticDTO;
import com.fifa.app.Entities.DurationUnit;
import com.fifa.app.Entities.PlayingTime;
import com.fifa.app.Mapper.PlayerStatisticMapper;
import com.fifa.app.dataSource.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Repository
@AllArgsConstructor
public class PlayerStatisticDAO {

    private final DataSource dataSource;

    public PlayerStatisticDTO findByPlayerIdAndSeasonYear(String playerId, int seasonYear) {
        String query = """
            SELECT ps.scored_goal, ps.playing_time
            FROM player_statistic ps
            JOIN season s ON ps.season_id = s.id
            WHERE ps.player_id = ?::uuid AND s.year = ?
        """;

        PlayerStatisticDTO playerStatistic = null;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setObject(1, playerId);
            statement.setInt(2, seasonYear);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    playerStatistic = PlayerStatisticMapper.mapFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des statistiques", e);
        }
        return playerStatistic;
    }
}
