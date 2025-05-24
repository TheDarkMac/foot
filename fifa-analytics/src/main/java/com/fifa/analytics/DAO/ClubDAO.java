package com.fifa.analytics.DAO;

import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.ClubStat;
import com.fifa.analytics.Enum.Championship;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Repository
public class ClubDAO {

    private DataConnection dataConnection;
    private ClubStatDAO clubStatDAO;

    public List<Club> getAll() {
        List<Club> clubs = new ArrayList<>();
        String query = "SELECT id, name, acronym,year_creation, stadium, championship  FROM clubs";
        try (Connection connection = dataConnection.getConnection()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                Club club = mapFromResultSet(resultSet);
                clubs.add(club);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return clubs;
    }

    public Club getClub(String clubId) {
        String query = "SELECT id, name, acronym,year_creation, stadium,championship FROM clubs WHERE id = ?::UUID";
        try (Connection connection = dataConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, clubId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return  mapFromResultSet(resultSet);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Club> saveAll(List<Club> clubs) {
        List<Club> clubList = new ArrayList<>();
        String query =  "INSERT INTO clubs(id, name, acronym, year_creation,stadium,coach_id,championship)" +
                "VALUES (?::UUID,?,?,?,?,?::UUID,?)" +
                "ON CONFLICT (name) " +
                "DO UPDATE SET " +
                "acronym = EXCLUDED.acronym," +
                "year_creation = EXCLUDED.year_creation," +
                "stadium = EXCLUDED.stadium ," +
                "coach_id = EXCLUDED.coach_id " +
                "RETURNING id, name, acronym, year_creation,stadium,coach_id,championship";
        clubs.forEach((club) -> {
            try(Connection connection = dataConnection.getConnection()){
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, club.getId());
                preparedStatement.setString(2, club.getName());
                preparedStatement.setString(3, club.getAcronym());
                preparedStatement.setObject(4, club.getYearCreation());
                preparedStatement.setString(5, club.getStadium());
                preparedStatement.setString(6,club.getCoach().getId());
                preparedStatement.setObject(7,club.getChampionship().name(),Types.OTHER);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Club clubFromDB = mapFromResultSet(resultSet);
                    clubList.add(clubFromDB);
                }
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        });
        return clubList;
    }

    public Club mapFromResultSet(ResultSet resultSet) throws SQLException {
        List<ClubStat> clubStats = clubStatDAO.getClubStatsByClubId(resultSet.getString("id"));
        Club club = new Club();
        club.setId(resultSet.getString("id"));
        club.setName(resultSet.getString("name"));
        club.setAcronym(resultSet.getString("acronym"));
        club.setYearCreation(resultSet.getInt("year_creation"));
        club.setStadium(resultSet.getString("stadium"));
        club.setChampionship(Championship.valueOf(resultSet.getString("championship")));
        if(clubStats != null){
            club.setClubStats(clubStats);
        }
        return club;
    }
}
