package com.fifa.app.DAO;

import com.fifa.app.DTO.PlayerDTO;
import com.fifa.app.Entities.*;
import com.fifa.app.dataSource.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.fifa.app.Mapper.PlayerMapper.mapPlayerDtoFromResultSet;
import static com.fifa.app.Mapper.PlayerMapper.mapPlayerFromResultSet;

@Repository
@AllArgsConstructor
public class PlayerDAO {

    private final DataSource dataSource;

    public List<Player> findAll(PlayerCriteria criteria) {
        List<Player> playerList = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("""
        SELECT
            p.id AS player_id,
            p.name,
            p.age,
            p.number,
            p.player_position,
            p.nationality,
            c.id AS club_id,
            c.name AS club_name,
            c.acronym,
            c.year_creation,
            c.stadium,
            co.id AS coach_id,
            co.name AS coach_name,
            co.nationality AS coach_nationality
        FROM players p
        LEFT JOIN club_player cp ON p.id = cp.player_id
        LEFT JOIN club c ON cp.club_id = c.id
        LEFT JOIN coach co ON c.coach_id = co.id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            queryBuilder.append(" AND p.name ILIKE ?");
            params.add("%" + criteria.getName() + "%");
        }
        if (criteria.getAgeMinimum() != null) {
            queryBuilder.append(" AND p.age >= ?");
            params.add(criteria.getAgeMinimum());
        }
        if (criteria.getAgeMaximum() != null) {
            queryBuilder.append(" AND p.age <= ?");
            params.add(criteria.getAgeMaximum());
        }
        if (criteria.getClubName() != null && !criteria.getClubName().isEmpty()) {
            queryBuilder.append(" AND c.name ILIKE ?");
            params.add("%" + criteria.getClubName() + "%");
        }

        String query = queryBuilder.toString();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)
        ) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Player player = mapPlayerFromResultSet(resultSet);
                playerList.add(player);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des joueurs", e);
        }

        return playerList;
    }

    public List<Player> createOrUpdatePlayers(List<Player> players) {
        List<Player> resultList = new ArrayList<>();

        String upsertQuery = """
        INSERT INTO players (id, name, number, player_position, nationality, age)
        VALUES (?::uuid, ?, ?, ?::\"position\", ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            number = EXCLUDED.number,
            player_position = EXCLUDED.player_position,
            nationality = EXCLUDED.nationality,
            age = EXCLUDED.age;
    """;

        try (Connection connection = dataSource.getConnection()) {
            for (Player player : players) {
                try (PreparedStatement stmt = connection.prepareStatement(upsertQuery)) {
                    stmt.setObject(1, player.getId());
                    stmt.setString(2, player.getName());
                    stmt.setInt(3, player.getNumber());
                    stmt.setString(4, player.getPosition().name());
                    stmt.setString(5, player.getNationality().name());
                    stmt.setInt(6, player.getAge());

                    stmt.executeUpdate();
                    resultList.add(player);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création ou mise à jour des joueurs", e);
        }

        return resultList;
    }

    public List<PlayerDTO> findPlayersByClubId(String clubId) {
        List<PlayerDTO> players = new ArrayList<>();

        String query = """
        SELECT p.id AS player_id, p.name, p.number, p.player_position, p.nationality, p.age
        FROM players p
        INNER JOIN club_player cp ON p.id = cp.player_id
        WHERE cp.club_id = ?::uuid;
    """;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)
        ) {
            stmt.setObject(1, clubId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlayerDTO player = mapPlayerDtoFromResultSet(rs);
                    players.add(player);
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des joueurs du club", e);
        }

        return players;
    }

}
