package com.fifa.analytics.DAO;

import com.fifa.analytics.DTO.ClubStat;
import com.fifa.analytics.DTO.Season;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Repository
public class ClubStatDAO {
    private DataConnection dataConnection;
    private SeasonDAO seasonDAO;

    public List<ClubStat> saveAll(List<ClubStat> clubStatList) {
        List<ClubStat> clubStats = new ArrayList<>();
        String query = "INSERT INTO club_statistics(club_id,season_year,ranking_points,scored_goals,difference_goals,clean_sheet_number)" +
                "VALUES(?::UUID,?,?,?,?,?)" +
                "ON CONFLICT (season_year,club_id) DO UPDATE SET " +
                "ranking_points = EXCLUDED.ranking_points," +
                "scored_goals = EXCLUDED.scored_goals," +
                "difference_goals = EXCLUDED.difference_goals," +
                "clean_sheet_number = EXCLUDED.clean_sheet_number " +
                "RETURNING club_id,season_year,ranking_points,scored_goals,difference_goals,clean_sheet_number";
        clubStatList.forEach(clubStat -> {
            try (Connection connection = dataConnection.getConnection()){
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1,clubStat.getClub().getId());
                preparedStatement.setInt(2,clubStat.getSeason().getYear());
                preparedStatement.setInt(3,clubStat.getRankingPoints());
                preparedStatement.setInt(4,clubStat.getScoredGoals());
                preparedStatement.setInt(5,clubStat.getDifferenceGoals());
                preparedStatement.setInt(6,clubStat.getCleanSheetNumber());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    ClubStat clubStat1 = mapFromResultSet(resultSet);
                    clubStats.add(clubStat1);
                }
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
        });
        return clubStats;
    }

    public List<ClubStat> getClubStatsByClubId(String clubId) {
        List<ClubStat> clubStats = new ArrayList<ClubStat>();
        String sql = "SELECT club_id,season_year,ranking_points,scored_goals,difference_goals,clean_sheet_number FROM club_statistics WHERE club_id = ?";
        try (Connection conn = dataConnection.getConnection();){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, clubId, Types.OTHER);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ClubStat clubStat = mapFromResultSet(rs);
                clubStats.add(clubStat);
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return clubStats;
    }

    public ClubStat mapFromResultSet(ResultSet resultSet) throws SQLException {
        Season season = seasonDAO.getSeason(resultSet.getInt("season_year"));
        ClubStat clubStat = new ClubStat();
        clubStat.setSeason(season);
        clubStat.setRankingPoints(resultSet.getInt("ranking_points"));
        clubStat.setScoredGoals(resultSet.getInt("scored_goals"));
        clubStat.setDifferenceGoals(resultSet.getInt("difference_goals"));
        clubStat.setCleanSheetNumber(resultSet.getInt("clean_sheet_number"));
        return clubStat;

    }
}
