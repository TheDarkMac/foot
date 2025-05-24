package com.fifa.analytics.DAO;

import com.fifa.analytics.DTO.Player;
import com.fifa.analytics.DTO.PlayerStatistics;
import com.fifa.analytics.Enum.Position;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Repository
public class PlayerDAO {

    private DataConnection dataConnection;
    private ClubDAO clubDAO;
    private PlayerStatisticsDAO playerStatisticsDAO;

    public Player getById(String id) {
        String query = "SELECT id,name,number,position,nationality," +
                "age,club_id FROM players WHERE id=?";
        try (Connection connection = dataConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setObject(1, id,Types.OTHER);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return  mapFromResultSet(resultSet);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Player> getAllPlayers() {
        String query = "SELECT id,name,number,position,nationality," +
                "age,club_id FROM players";

        List<Player> players = new ArrayList<>();
        try (Connection connection = dataConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Player player = mapFromResultSet(resultSet);
                players.add(player);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return players;
    }

    public List<Player> saveAll(List<Player> players) {
        List<Player> savedPlayers = new ArrayList<>();
        String query = "INSERT INTO players (id, name, number, nationality, position, age, club_id) " +
                "VALUES (?::UUID, ?, ?, ?, ?, ?, ?::UUID) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "name = EXCLUDED.name, " +
                "number = EXCLUDED.number, " +
                "nationality = EXCLUDED.nationality, " +
                "position = EXCLUDED.position, " +
                "age = EXCLUDED.age, " +
                "club_id = EXCLUDED.club_id " +
                "RETURNING id,name,number,nationality,position,age,club_id";
        players.forEach(player -> {
            try(Connection connection = dataConnection.getConnection()){

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, player.getId());
                preparedStatement.setString(2, player.getName());
                preparedStatement.setInt(3,player.getNumber());
                preparedStatement.setString(4,player.getNationality());
                preparedStatement.setObject(5, player.getPosition().name(),Types.OTHER);
                preparedStatement.setInt(6,player.getAge());
                preparedStatement.setString(7,player.getClub().getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    Player p = mapFromResultSet(resultSet);
                    savedPlayers.add(p);
                }
            }catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return savedPlayers;
    }

    private Player mapFromResultSet(ResultSet resultSet) throws SQLException {
        List<PlayerStatistics> playerStatistics = playerStatisticsDAO.getPlayerStatistics(resultSet.getString("id"));
        Player player = new Player();
        player.setId(resultSet.getString("id"));
        player.setName(resultSet.getString("name"));
        player.setNumber(resultSet.getInt("number"));
        player.setPosition(Position.valueOf(resultSet.getString("position")));
        player.setNationality(resultSet.getString("nationality"));
        player.setAge(resultSet.getInt("age"));
        player.setClub(clubDAO.getClub(resultSet.getString("club_id")));
        if (playerStatistics != null) {
            player.setPlayerStatistics(playerStatistics);
        }
        return player;
    }

    public Mono<Player> getByIdReactive(String playerId) {
        return Mono.fromCallable(() -> getById(playerId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
