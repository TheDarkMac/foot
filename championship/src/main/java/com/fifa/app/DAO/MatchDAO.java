package com.fifa.app.DAO;

import com.fifa.app.DTO.MatchDisplayDTO;
import com.fifa.app.DTO.PlayerScorerDTO;
import com.fifa.app.Entities.*;
import com.fifa.app.dataSource.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class MatchDAO {

    private final DataSource dataSource;

    public List<MatchDisplayDTO> createMatchesForSeason(int seasonYear) {
        List<MatchDisplayDTO> createdMatches = new ArrayList<>();


        String seasonQuery = "SELECT * FROM season WHERE year = ?";
        String checkMatchQuery = "SELECT COUNT(*) FROM match WHERE season_year = ?";
        String clubQuery = "SELECT * FROM club";
        String insertMatchQuery = """
        INSERT INTO match (id, club_playing_home_id, club_playing_away_id, stadium, match_datetime, actual_status, season_year)
        VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?, ?::match_status, ?)
    """;

        try (Connection connection = dataSource.getConnection()) {

            Season season = null;
            try (PreparedStatement stmt = connection.prepareStatement(seasonQuery)) {
                stmt.setInt(1, seasonYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        season = new Season(
                            rs.getObject("id").toString(),
                            rs.getInt("year"),
                            rs.getString("alias"),
                            SeasonStatus.valueOf(rs.getString("status"))
                        );
                    } else {
                        throw new RuntimeException("Saison " + seasonYear + " introuvable.");
                    }
                }
            }

            if (season.getStatus() != SeasonStatus.STARTED) {
                throw new IllegalArgumentException("La saison doit être STARTED.");
            }


            try (PreparedStatement stmt = connection.prepareStatement(checkMatchQuery)) {
                stmt.setInt(1, seasonYear);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new IllegalArgumentException("Les matchs sont déjà générés.");
                    }
                }
            }


            List<Club> clubs = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(clubQuery);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Club club = new Club(
                        rs.getObject("id").toString(),
                        rs.getString("name"),
                        rs.getString("acronym")
                    );
                    clubs.add(club);
                }
            }


            try (PreparedStatement stmt = connection.prepareStatement(insertMatchQuery)) {
                for (int i = 0; i < clubs.size(); i++) {
                    for (int j = 0; j < clubs.size(); j++) {
                        if (i != j) {
                            String matchId = UUID.randomUUID().toString();
                            Club home = clubs.get(i);
                            Club away = clubs.get(j);
                            String stadium = "Stadium of " + home.getName();
                            LocalDateTime matchTime = LocalDateTime.now().plusDays(i + j);

                            // Insertion en DB
                            stmt.setObject(1, matchId);
                            stmt.setObject(2, UUID.fromString(home.getId()));
                            stmt.setObject(3, UUID.fromString(away.getId()));
                            stmt.setString(4, stadium);
                            stmt.setTimestamp(5, Timestamp.valueOf(matchTime));
                            stmt.setString(6, MatchStatus.NOT_STARTED.name());
                            stmt.setInt(7, seasonYear);
                            stmt.executeUpdate();


                            ClubPlaying homeClub = new ClubPlaying();
                            homeClub.setId(home.getId());
                            homeClub.setName(home.getName());
                            homeClub.setAcronym(home.getAcronym());
                            homeClub.setScore(0);
                            homeClub.setScorers(new ArrayList<>());

                            ClubPlaying awayClub = new ClubPlaying();
                            awayClub.setId(away.getId());
                            awayClub.setName(away.getName());
                            awayClub.setAcronym(away.getAcronym());
                            awayClub.setScore(0);
                            awayClub.setScorers(new ArrayList<>());


                            MatchDisplayDTO matchDto = new MatchDisplayDTO();
                            matchDto.setId(matchId);
                            matchDto.setClubPlayingHome(homeClub);
                            matchDto.setClubPlayingAway(awayClub);
                            matchDto.setStadium(stadium);
                            matchDto.setMatchDatetime(matchTime.toLocalDate());
                            matchDto.setActualStatus(MatchStatus.NOT_STARTED);

                            createdMatches.add(matchDto);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération des matchs", e);
        }

        return createdMatches;
    }

    public List<MatchDisplayDTO> findMatchesFiltered(int seasonYear, String matchStatus, String clubPlayingName,
                                                     LocalDateTime matchAfter, LocalDateTime matchBeforeOrEquals) {
        List<MatchDisplayDTO> matches = new ArrayList<>();

        StringBuilder query = new StringBuilder("""
        SELECT m.id as match_id, m.stadium, m.match_datetime, m.actual_status,
                                              m.home_score, m.away_score,
                                              home.id as home_id, home.name as home_name, home.acronym as home_acronym,
                                              away.id as away_id, away.name as away_name, away.acronym as away_acronym
                                       FROM match m
                                       JOIN club home ON m.club_playing_home_id = home.id
                                       JOIN club away ON m.club_playing_away_id = away.id
                                       WHERE m.season_year = ?
    """);

        List<Object> params = new ArrayList<>();
        params.add(seasonYear);

        if (matchStatus != null) {
            query.append(" AND m.actual_status = ?::match_status");
            params.add(matchStatus);
        }

        if (clubPlayingName != null) {
            query.append(" AND (home.name ILIKE ? OR away.name ILIKE ?)");
            params.add("%" + clubPlayingName + "%");
            params.add("%" + clubPlayingName + "%");
        }

        if (matchAfter != null) {
            query.append(" AND m.match_datetime > ?");
            params.add(Timestamp.valueOf(matchAfter));
        }

        if (matchBeforeOrEquals != null) {
            query.append(" AND m.match_datetime <= ?");
            params.add(Timestamp.valueOf(matchBeforeOrEquals));
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String matchId = rs.getString("match_id");
                    String stadium = rs.getString("stadium");
                    LocalDate matchDate = rs.getTimestamp("match_datetime").toLocalDateTime().toLocalDate();
                    MatchStatus status = MatchStatus.valueOf(rs.getString("actual_status"));

                    ClubPlaying home = new ClubPlaying();
                    home.setId(rs.getString("home_id"));
                    home.setName(rs.getString("home_name"));
                    home.setAcronym(rs.getString("home_acronym"));
                    int homeScore = rs.getInt("home_score");
                    int awayScore = rs.getInt("away_score");
                    home.setScore(homeScore);

                    home.setScorers(getScorersForClubInMatch(conn, matchId, home.getId()));

                    ClubPlaying away = new ClubPlaying();
                    away.setId(rs.getString("away_id"));
                    away.setName(rs.getString("away_name"));
                    away.setAcronym(rs.getString("away_acronym"));
                    away.setScore(awayScore);
                    away.setScorers(getScorersForClubInMatch(conn, matchId, away.getId()));

                    MatchDisplayDTO dto = new MatchDisplayDTO();
                    dto.setId(matchId);
                    dto.setStadium(stadium);
                    dto.setMatchDatetime(matchDate);
                    dto.setActualStatus(status);
                    dto.setClubPlayingHome(home);
                    dto.setClubPlayingAway(away);

                    matches.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du filtrage des matchs", e);
        }

        return matches;
    }

    private List<Scorer> getScorersForClubInMatch(Connection conn, String matchId, String clubId) throws SQLException {
        String query = """
        SELECT g.minute_of_goal, g.own_goal,
               p.id as player_id, p.name, p.age, p.number
        FROM goal g
        JOIN players p ON g.player_id = p.id
        WHERE g.match_id = ?::uuid AND g.club_id = ?::uuid
    """;

        List<Scorer> scorers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, matchId);
            stmt.setString(2, clubId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlayerScorerDTO player = new PlayerScorerDTO();
                    player.setId(rs.getString("player_id"));
                    player.setName(rs.getString("name"));
                    player.setNumber(rs.getInt("number"));

                    Scorer scorer = new Scorer();
                    scorer.setPlayer(player);
                    scorer.setMinuteOfGoal(rs.getInt("minute_of_goal"));
                    scorer.setOwnGoal(rs.getBoolean("own_goal"));

                    scorers.add(scorer);
                }
            }
        }

        return scorers;
    }
    /**
     * option 1 avec club stat function
     *
     * option came pre-fix
     */

    public MatchDisplayDTO updateMatchStatus(UUID matchId, String newStatusStr) {
        MatchStatus newStatus = MatchStatus.valueOf(newStatusStr);
        MatchDisplayDTO updatedMatch = null;

        String selectQuery = """
        SELECT m.actual_status, m.club_playing_home_id, m.club_playing_away_id,
               m.home_score, m.away_score, m.stadium, m.match_datetime,
               home.name as home_name, home.acronym as home_acronym,
               away.name as away_name, away.acronym as away_acronym
        FROM match m
        JOIN club home ON m.club_playing_home_id = home.id
        JOIN club away ON m.club_playing_away_id = away.id
        WHERE m.id = ?
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setObject(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Match not found");

                MatchStatus currentStatus = MatchStatus.valueOf(rs.getString("actual_status"));
                UUID homeId = (UUID) rs.getObject("club_playing_home_id");
                UUID awayId = (UUID) rs.getObject("club_playing_away_id");
                int homeScore = rs.getInt("home_score");
                int awayScore = rs.getInt("away_score");


                if (!isValidStatusTransition(currentStatus, newStatus)) {
                    throw new IllegalStateException("Invalid status transition");
                }


                String updateStatusSql = "UPDATE match SET actual_status = ?::match_status WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStatusSql)) {
                    updateStmt.setString(1, newStatus.name());
                    updateStmt.setObject(2, matchId);
                    updateStmt.executeUpdate();
                }


                if (newStatus == MatchStatus.FINISHED) {
                    updateClubStatistics(conn, homeId, awayId, homeScore, awayScore);
                }


                updatedMatch = new MatchDisplayDTO();
                updatedMatch.setId(matchId.toString());
                updatedMatch.setStadium(rs.getString("stadium"));
                updatedMatch.setMatchDatetime(LocalDate.from(rs.getTimestamp("match_datetime").toLocalDateTime()));
                updatedMatch.setActualStatus(newStatus);

                ClubPlaying home = new ClubPlaying();
                home.setId(homeId.toString());
                home.setAcronym(rs.getString("home_acronym"));
                home.setName(rs.getString("home_name"));
                home.setScore(homeScore);

                ClubPlaying away = new ClubPlaying();
                away.setId(awayId.toString());
                away.setAcronym(rs.getString("away_acronym"));
                away.setName(rs.getString("away_name"));
                away.setScore(awayScore);

                home.setScorers(new ArrayList<>());
                away.setScorers(new ArrayList<>());

                updatedMatch.setClubPlayingHome(home);
                updatedMatch.setClubPlayingAway(away);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du statut du match", e);
        }

        return updatedMatch;
    }

    /**
     * option 2 : sans update club stat function
     *
     * option 2 came post-fix
     * @param matchId
     * @param newStatusStr
     * @return
     */
    /*public MatchDisplayDTO updateMatchStatus(UUID matchId, String newStatusStr) {
        MatchStatus newStatus = MatchStatus.valueOf(newStatusStr);
        MatchDisplayDTO updatedMatch = null;

        String selectQuery = """
    SELECT m.actual_status, m.club_playing_home_id, m.club_playing_away_id,
           m.home_score, m.away_score, m.stadium, m.match_datetime,
           home.name as home_name, home.acronym as home_acronym,
           away.name as away_name, away.acronym as away_acronym
    FROM match m
    JOIN club home ON m.club_playing_home_id = home.id
    JOIN club away ON m.club_playing_away_id = away.id
    WHERE m.id = ?
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectQuery)) {

            stmt.setObject(1, matchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Match not found");

                MatchStatus currentStatus = MatchStatus.valueOf(rs.getString("actual_status"));
                UUID homeId = (UUID) rs.getObject("club_playing_home_id");
                UUID awayId = (UUID) rs.getObject("club_playing_away_id");
                int homeScore = rs.getInt("home_score");
                int awayScore = rs.getInt("away_score");

                if (!isValidStatusTransition(currentStatus, newStatus)) {
                    throw new IllegalStateException("Invalid status transition");
                }

                String updateStatusSql = "UPDATE match SET actual_status = ?::match_status WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateStatusSql)) {
                    updateStmt.setString(1, newStatus.name());
                    updateStmt.setObject(2, matchId);
                    updateStmt.executeUpdate();
                }

                // Supprimer l'appel à updateClubStatistics
                // Les stats sont déjà mises à jour dans MatchGoalDAO

                updatedMatch = new MatchDisplayDTO();
                updatedMatch.setId(matchId.toString());
                updatedMatch.setStadium(rs.getString("stadium"));
                updatedMatch.setMatchDatetime(LocalDate.from(rs.getTimestamp("match_datetime").toLocalDateTime()));
                updatedMatch.setActualStatus(newStatus);

                ClubPlaying home = new ClubPlaying();
                home.setId(homeId.toString());
                home.setAcronym(rs.getString("home_acronym"));
                home.setName(rs.getString("home_name"));
                home.setScore(homeScore);

                ClubPlaying away = new ClubPlaying();
                away.setId(awayId.toString());
                away.setAcronym(rs.getString("away_acronym"));
                away.setName(rs.getString("away_name"));
                away.setScore(awayScore);

                home.setScorers(new ArrayList<>());
                away.setScorers(new ArrayList<>());

                updatedMatch.setClubPlayingHome(home);
                updatedMatch.setClubPlayingAway(away);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du statut du match", e);
        }

        return updatedMatch;
    }*/


    private boolean isValidStatusTransition(MatchStatus current, MatchStatus next) {
        List<MatchStatus> order = List.of(MatchStatus.NOT_STARTED, MatchStatus.STARTED, MatchStatus.FINISHED);
        return order.indexOf(next) > order.indexOf(current);
    }

    /**
     * option 1 fecth all data , may happen that there will be a second update on database ,
     * leading to a wrong result ,
     *
     * dev can keep this methode (option 1) but must change things inside mathcGoalDAO
     * especially , handle update score and statistic
     *
     * option came pre-fix
     * @param conn
     * @param homeId
     * @param awayId
     * @param homeScore
     * @param awayScore
     * @throws SQLException
     */
    /*private void updateClubStatistics(Connection conn, UUID homeId, UUID awayId, int homeScore, int awayScore) throws SQLException {
        int homePoints = 0, awayPoints = 0;
        if (homeScore > awayScore) {
            homePoints = 3;
        } else if (awayScore > homeScore) {
            awayPoints = 3;
        } else {
            homePoints = 1;
            awayPoints = 1;
        }


        String updateStatsSql = """
        UPDATE club_statistic
        SET rankingPoints = COALESCE(rankingPoints,0) + ?,
            scoredGoals = COALESCE(scoredGoals,0) + ?,
            concededGoals = COALESCE(concededGoals,0) + ?,
            differenceGoals = COALESCE(differenceGoals,0) + (? - ?),
            cleanSheetNumber = cleanSheetNumber + (CASE WHEN ? = 0 THEN 1 ELSE 0 END)
        WHERE club_id = ?
    """;

        try (PreparedStatement updateHome = conn.prepareStatement(updateStatsSql);
             PreparedStatement updateAway = conn.prepareStatement(updateStatsSql)) {

            updateHome.setInt(1, homePoints);
            updateHome.setInt(2, homeScore);
            updateHome.setInt(3, awayScore);
            updateHome.setInt(4, homeScore);
            updateHome.setInt(5, awayScore);
            updateHome.setInt(6, awayScore);
            updateHome.setObject(7, homeId);
            updateHome.executeUpdate();

            updateAway.setInt(1, awayPoints);
            updateAway.setInt(2, awayScore);
            updateAway.setInt(3, homeScore);
            updateAway.setInt(4, awayScore);
            updateAway.setInt(5, homeScore);
            updateAway.setInt(6, homeScore);
            updateAway.setObject(7, awayId);
            updateAway.executeUpdate();
        }
    }*/

    /**
     * option 2 ! no score or conced goal update , this mean ,
     * dev don't need to change matchGoalDAO
     *
     * option 2 came post-fix
     */
    private void updateClubStatistics(Connection conn, UUID homeId, UUID awayId, int homeScore, int awayScore) throws SQLException {
        int homePoints = 0, awayPoints = 0;
        if (homeScore > awayScore) {
            homePoints = 3;
        } else if (awayScore > homeScore) {
            awayPoints = 3;
        } else {
            homePoints = 1;
            awayPoints = 1;
        }

        String updateStatsSql = """
    UPDATE club_statistic
    SET rankingPoints = COALESCE(rankingPoints,0) + ?,
        differenceGoals = COALESCE(differenceGoals,0) + (? - ?),
        cleanSheetNumber = cleanSheetNumber + (CASE WHEN ? = 0 THEN 1 ELSE 0 END)
    WHERE club_id = ?
    """;

        try (PreparedStatement updateHome = conn.prepareStatement(updateStatsSql);
             PreparedStatement updateAway = conn.prepareStatement(updateStatsSql)) {

            // Mise à jour pour l'équipe à domicile
            updateHome.setInt(1, homePoints);
            updateHome.setInt(2, homeScore);  // buts marqués pour différence
            updateHome.setInt(3, awayScore);  // buts encaissés pour différence
            updateHome.setInt(4, awayScore);  // pour clean sheet (si adversaire a marqué 0)
            updateHome.setObject(5, homeId);
            updateHome.executeUpdate();

            // Mise à jour pour l'équipe à l'extérieur
            updateAway.setInt(1, awayPoints);
            updateAway.setInt(2, awayScore);  // buts marqués pour différence
            updateAway.setInt(3, homeScore);  // buts encaissés pour différence
            updateAway.setInt(4, homeScore);  // pour clean sheet (si adversaire a marqué 0)
            updateAway.setObject(5, awayId);
            updateAway.executeUpdate();
        }
    }

    public MatchDisplayDTO findMatchById(UUID matchId) {
        String query = """
        SELECT
            m.id as match_id,
            m.stadium,
            m.match_datetime,
            m.actual_status,
            m.home_score,
            m.away_score,
            home.id as home_id,
            home.name as home_name,
            home.acronym as home_acronym,
            away.id as away_id,
            away.name as away_name,
            away.acronym as away_acronym
        FROM match m
        JOIN club home ON m.club_playing_home_id = home.id
        JOIN club away ON m.club_playing_away_id = away.id
        WHERE m.id = ?::uuid
    """;

        String scorersQuery = """
        SELECT
            g.id as goal_id,
            g.club_id,
            g.player_id,
            p.name as player_name,
            p.number as player_number,
            g.minute_of_goal,
            g.own_goal
        FROM goal g
        JOIN players p ON g.player_id = p.id
        WHERE g.match_id = ?::uuid
        ORDER BY g.minute_of_goal
    """;

        try (Connection conn = dataSource.getConnection()) {

            MatchDisplayDTO matchDto;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setObject(1, matchId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Match not found");
                    }

                    ClubPlaying homeClub = new ClubPlaying();
                    homeClub.setId(rs.getString("home_id"));
                    homeClub.setName(rs.getString("home_name"));
                    homeClub.setAcronym(rs.getString("home_acronym"));
                    homeClub.setScore(rs.getInt("home_score"));
                    homeClub.setScorers(new ArrayList<>());

                    ClubPlaying awayClub = new ClubPlaying();
                    awayClub.setId(rs.getString("away_id"));
                    awayClub.setName(rs.getString("away_name"));
                    awayClub.setAcronym(rs.getString("away_acronym"));
                    awayClub.setScore(rs.getInt("away_score"));
                    awayClub.setScorers(new ArrayList<>());


                    matchDto = new MatchDisplayDTO();
                    matchDto.setId(rs.getString("match_id"));
                    matchDto.setStadium(rs.getString("stadium"));
                    matchDto.setMatchDatetime(rs.getTimestamp("match_datetime").toLocalDateTime().toLocalDate());
                    matchDto.setActualStatus(MatchStatus.valueOf(rs.getString("actual_status")));
                    matchDto.setClubPlayingHome(homeClub);
                    matchDto.setClubPlayingAway(awayClub);
                }
            }


            try (PreparedStatement stmt = conn.prepareStatement(scorersQuery)) {
                stmt.setObject(1, matchId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Scorer scorer = new Scorer();

                        // Création du joueur
                        PlayerScorerDTO player = new PlayerScorerDTO();
                        player.setId(rs.getString("player_id"));
                        player.setName(rs.getString("player_name"));
                        player.setNumber(rs.getInt("player_number"));
                        scorer.setPlayer(player);

                        scorer.setMinuteOfGoal(rs.getInt("minute_of_goal"));
                        scorer.setOwnGoal(rs.getBoolean("own_goal"));

                        // Ajouter au bon club (home ou away)
                        String clubId = rs.getString("club_id");
                        if (clubId.equals(matchDto.getClubPlayingHome().getId())) {
                            matchDto.getClubPlayingHome().getScorers().add(scorer);
                        } else if (clubId.equals(matchDto.getClubPlayingAway().getId())) {
                            // Si c'est un but contre son camp, l'ajouter à l'équipe adverse
                            if (scorer.isOwnGoal()) {
                                matchDto.getClubPlayingHome().getScorers().add(scorer);
                            } else {
                                matchDto.getClubPlayingAway().getScorers().add(scorer);
                            }
                        }
                    }
                }
            }

            return matchDto;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving match by ID", e);
        }
    }


}
