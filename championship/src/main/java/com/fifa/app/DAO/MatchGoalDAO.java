package com.fifa.app.DAO;

import com.fifa.app.DTO.MatchDisplayDTO;
import com.fifa.app.Entities.*;
import com.fifa.app.dataSource.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.UUID;

@Repository
public class MatchGoalDAO {
    private final DataSource dataSource;
    private final MatchDAO matchDAO;
    private final PlayerDAO playerDAO;

    public MatchGoalDAO(DataSource dataSource, MatchDAO matchDAO, PlayerDAO playerDAO) {
        this.dataSource = dataSource;
        this.matchDAO = matchDAO;
        this.playerDAO = playerDAO;
    }

    public MatchDisplayDTO addGoalsToMatch(String matchId, List<GoalRequest> goalRequests) {
        MatchDisplayDTO updatedMatch = null;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {

                String statusCheckQuery = "SELECT actual_status FROM match WHERE id = ?::uuid";
                MatchStatus matchStatus;

                try (PreparedStatement stmt = connection.prepareStatement(statusCheckQuery)) {
                    stmt.setString(1, matchId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Match not found");
                        }
                        matchStatus = MatchStatus.valueOf(rs.getString("actual_status"));
                    }
                }

                if (matchStatus != MatchStatus.STARTED) {
                    throw new IllegalArgumentException("Cannot add goals to a match that is not STARTED");
                }


                for (GoalRequest goalRequest : goalRequests) {
                    verifyPlayerClub(connection, goalRequest.getScorerIdentifier(), goalRequest.getClubId());
                    insertGoal(connection, matchId, goalRequest);

                    boolean isHomeTeam = isHomeTeam(connection, matchId, goalRequest.getClubId());
                    updateMatchScore(connection, matchId, goalRequest);
                    updatePlayerStatistics(connection, goalRequest);
                    updateClubStatistics(connection, goalRequest, isHomeTeam, matchId);
                }

                connection.commit();
                updatedMatch = matchDAO.findMatchById(UUID.fromString(matchId));

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error adding goals to match", e);
        }

        return updatedMatch;
    }

    private boolean isHomeTeam(Connection connection, String matchId, String clubId) throws SQLException {
        String query = "SELECT club_playing_home_id = ?::uuid AS is_home FROM match WHERE id = ?::uuid";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, clubId);
            stmt.setString(2, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("is_home");
                }
                throw new IllegalArgumentException("Club is not playing in this match");
            }
        }
    }

    private void verifyPlayerClub(Connection connection, String playerId, String clubId) throws SQLException {
        playerId = playerId.trim();
        clubId = clubId.trim();

        String query = "SELECT 1 FROM club_player WHERE player_id = ?::uuid AND club_id = ?::uuid";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, UUID.fromString(playerId));
            stmt.setObject(2, UUID.fromString(clubId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Player does not belong to the specified club");
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid UUID format", e);
        }
    }

    private void insertGoal(Connection connection, String matchId, GoalRequest goalRequest) throws SQLException {
        String query = """
            INSERT INTO goal (id, match_id, player_id, club_id, minute_of_goal, own_goal)
            VALUES (uuid_generate_v4(), ?::uuid, ?::uuid, ?::uuid, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, matchId);
            stmt.setString(2, goalRequest.getScorerIdentifier());
            stmt.setString(3, goalRequest.getClubId());
            stmt.setInt(4, goalRequest.getMinuteOfGoal());
            stmt.setBoolean(5, goalRequest.isOwnGoal());
            stmt.executeUpdate();
        }
    }

    private void updateMatchScore(Connection connection, String matchId, GoalRequest goalRequest) throws SQLException {
        String teamCheckQuery = """
            SELECT
                CASE WHEN club_playing_home_id = ?::uuid THEN 'home'
                     WHEN club_playing_away_id = ?::uuid THEN 'away'
                END AS team_side
            FROM match
            WHERE id = ?::uuid
        """;

        String teamSide;
        try (PreparedStatement stmt = connection.prepareStatement(teamCheckQuery)) {
            stmt.setString(1, goalRequest.getClubId());
            stmt.setString(2, goalRequest.getClubId());
            stmt.setString(3, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Club is not playing in this match");
                }
                teamSide = rs.getString("team_side");
                if (teamSide == null) {
                    throw new IllegalArgumentException("Club is not playing in this match");
                }
            }
        }

        String updateQuery = "home".equals(teamSide) ?
            "UPDATE match SET home_score = home_score + 1 WHERE id = ?::uuid" :
            "UPDATE match SET away_score = away_score + 1 WHERE id = ?::uuid";

        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setString(1, matchId);
            stmt.executeUpdate();
        }
    }

    private void updatePlayerStatistics(Connection connection, GoalRequest goalRequest) throws SQLException {
        if (goalRequest.isOwnGoal()) {
            return;
        }

        String checkQuery = "SELECT 1 FROM player_statistic WHERE player_id = ?::uuid AND season_id = (SELECT id FROM season WHERE status = 'STARTED' ORDER BY year DESC LIMIT 1)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, goalRequest.getScorerIdentifier());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String updateQuery = "UPDATE player_statistic SET scored_goal = scored_goal + 1 WHERE player_id = ?::uuid AND season_id = (SELECT id FROM season WHERE status = 'STARTED' ORDER BY year DESC LIMIT 1)";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, goalRequest.getScorerIdentifier());
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO player_statistic (id, player_id, scored_goal, playing_time, season_id) VALUES (uuid_generate_v4(), ?::uuid, 1, 0, (SELECT id FROM season WHERE status = 'STARTED' ORDER BY year DESC LIMIT 1))";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, goalRequest.getScorerIdentifier());
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private void updateClubStatistics(Connection connection, GoalRequest goalRequest, boolean isHomeTeam, String matchId) throws SQLException {
        String currentClubId = goalRequest.getClubId();
        String opposingClubId = getOpposingClubId(connection, currentClubId, matchId);

        // Récupération du season_year du match
        int seasonYear;
        String seasonQuery = "SELECT season_year FROM match WHERE id = ?::uuid";
        try (PreparedStatement stmt = connection.prepareStatement(seasonQuery)) {
            stmt.setString(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    seasonYear = rs.getInt("season_year");
                } else {
                    throw new IllegalArgumentException("Match not found to retrieve season_year");
                }
            }
        }

        // Mise à jour du club ayant marqué
        updateClubScoredGoals(connection, currentClubId, seasonYear);

        // Mise à jour du club adverse (buts encaissés)
        updateClubConcededGoals(connection, opposingClubId, seasonYear);
    }

    private String getOpposingClubId(Connection connection, String currentClubId, String matchId) throws SQLException {
        String query = """
            SELECT
                CASE
                    WHEN club_playing_home_id = ?::uuid THEN club_playing_away_id
                    WHEN club_playing_away_id = ?::uuid THEN club_playing_home_id
                    ELSE NULL
                END AS opposing_club_id
            FROM match
            WHERE id = ?::uuid
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentClubId);
            stmt.setString(2, currentClubId);
            stmt.setString(3, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String opposingClubId = rs.getString("opposing_club_id");
                    if (opposingClubId == null) {
                        throw new IllegalArgumentException("Le club spécifié ne participe pas à ce match");
                    }
                    return opposingClubId;
                } else {
                    throw new IllegalArgumentException("Match non trouvé avec l'ID: " + matchId);
                }
            }
        }
    }

    private void updateClubScoredGoals(Connection connection, String clubId, int seasonYear) throws SQLException {
        String checkQuery = "SELECT 1 FROM club_statistic WHERE club_id = ?::uuid AND season_year = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, clubId);
            checkStmt.setInt(2, seasonYear);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String updateQuery = "UPDATE club_statistic SET scoredgoals = scoredgoals + 1 WHERE club_id = ?::uuid AND season_year = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, clubId);
                        updateStmt.setInt(2, seasonYear);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO club_statistic (id, club_id, scoredgoals, concededgoals, season_year) VALUES (uuid_generate_v4(), ?::uuid, 1, 0, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, clubId);
                        insertStmt.setInt(2, seasonYear);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    private void updateClubConcededGoals(Connection connection, String clubId, int seasonYear) throws SQLException {
        String checkQuery = "SELECT 1 FROM club_statistic WHERE club_id = ?::uuid AND season_year = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, clubId);
            checkStmt.setInt(2, seasonYear);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    String updateQuery = "UPDATE club_statistic SET concededgoals = concededgoals + 1 WHERE club_id = ?::uuid AND season_year = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, clubId);
                        updateStmt.setInt(2, seasonYear);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO club_statistic (id, club_id, scoredgoals, concededgoals, season_year) VALUES (uuid_generate_v4(), ?::uuid, 0, 1, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, clubId);
                        insertStmt.setInt(2, seasonYear);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }
}
