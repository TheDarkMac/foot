package com.fifa.app.DAO;

import com.fifa.app.DTO.ClubDTO;
import com.fifa.app.DTO.ClubStatisticDTO;
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
import java.util.UUID;

@Repository
@AllArgsConstructor
public class ClubDAO {

    private final DataSource dataSource;

    public List<Club> findAllClubs() {
        List<Club> clubList = new ArrayList<>();
        String query = """
        SELECT c.id, c.name, c.acronym, c.year_creation, c.stadium,
               co.id AS coach_id, co.name AS coach_name, co.nationality AS coach_nationality
        FROM club c
        JOIN coach co ON c.coach_id = co.id
        """;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Club club = mapFromResultSet(resultSet);
                clubList.add(club);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des clubs", e);
        }

        return clubList;
    }

    private Club mapFromResultSet(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setId(rs.getObject("id").toString());
        club.setName(rs.getString("name"));
        club.setAcronym(rs.getString("acronym"));
        club.setYearCreation(rs.getInt("year_creation"));
        club.setStadium(rs.getString("stadium"));

        // Création de l'entité Coach à partir des données de la base
        Coach coach = new Coach();
        coach.setId(rs.getObject("coach_id").toString());
        coach.setName(rs.getString("coach_name"));
        coach.setNationality(Nationality.valueOf(rs.getString("coach_nationality") != null
            ? rs.getString("coach_nationality") : "UNKNOWN"));
        club.setCoach(coach);

        return club;
    }

    public List<Club> createOrUpdateClubs(List<Club> clubs) {
        String insertQuery = """
        INSERT INTO club (id, name, acronym, year_creation, stadium, coach_id)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO UPDATE SET
            name = EXCLUDED.name,
            acronym = EXCLUDED.acronym,
            year_creation = EXCLUDED.year_creation,
            stadium = EXCLUDED.stadium,
            coach_id = EXCLUDED.coach_id
        """;

        String findCoachQuery = "SELECT id FROM coach WHERE name = ? AND nationality = ?";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement coachStmt = connection.prepareStatement(findCoachQuery);
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery)
        ) {
            for (Club club : clubs) {
                Coach coach = club.getCoach();
                // Trouver l'ID du coach en base
                coachStmt.setString(1, coach.getName());
                coachStmt.setString(2, coach.getNationality().name());
                ResultSet rs = coachStmt.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Coach introuvable : " + coach.getName() + ", " + coach.getNationality());
                }
                String coachId = rs.getObject("id").toString();

                // Préparer l'insert/update du club
                insertStmt.setObject(1, UUID.fromString(club.getId()));
                insertStmt.setString(2, club.getName());
                insertStmt.setString(3, club.getAcronym());
                insertStmt.setInt(4, club.getYearCreation());
                insertStmt.setString(5, club.getStadium());
                insertStmt.setObject(6, UUID.fromString(coachId));
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création ou mise à jour des clubs", e);
        }
        return clubs;
    }

    public List<PlayerDTO> updateClubPlayers(UUID clubId, List<PlayerDTO> players) {
        String deleteQuery = """
    DELETE FROM club_player WHERE club_id = ?::uuid;
    """;

        String insertClubPlayerQuery = """
    INSERT INTO club_player (id, club_id, player_id)
    VALUES (?::uuid, ?::uuid, ?::uuid);
    """;

        String deletePlayerFromOldClubQuery = """
    DELETE FROM club_player WHERE player_id = ?::uuid;
    """;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (
                PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
                PreparedStatement insertStmt = connection.prepareStatement(insertClubPlayerQuery);
                PreparedStatement deleteOldClubStmt = connection.prepareStatement(deletePlayerFromOldClubQuery);
            ) {
                // 1. Supprimer tous les joueurs du club
                deleteStmt.setObject(1, clubId);
                deleteStmt.executeUpdate();

                // 2. Ajouter les nouveaux joueurs au club
                for (PlayerDTO player : players) {
                    // Vérifier si le joueur est déjà dans un autre club
                    String checkPlayerQuery = "SELECT club_id FROM club_player WHERE player_id = ?::uuid";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkPlayerQuery)) {
                        checkStmt.setObject(1, UUID.fromString(player.getId()));
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                UUID existingClubId = UUID.fromString(rs.getString("club_id"));
                                if (!existingClubId.equals(clubId)) {
                                    // Si le joueur est déjà dans un autre club, on le supprime de ce club
                                    deleteOldClubStmt.setObject(1, UUID.fromString(player.getId()));
                                    deleteOldClubStmt.executeUpdate();
                                }
                            }

                            // Ensuite, on ajoute le joueur au nouveau club
                            insertStmt.setObject(1, UUID.randomUUID());
                            insertStmt.setObject(2, clubId);
                            insertStmt.setObject(3, UUID.fromString(player.getId()));
                            insertStmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Erreur lors de la mise à jour des joueurs du club", e);
            }

            return players;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public List<PlayerDTO> addPlayersToClub(UUID clubId, List<PlayerDTO> players) {
        String checkPlayerExistQuery = "SELECT * FROM players WHERE id = ?::uuid";
        String insertPlayerQuery = """
        INSERT INTO players (id, name, number, player_position, nationality, age)
        VALUES (?::uuid, ?, ?, ?::\"position\", ?, ?)
    """;
        String checkIfPlayerInClubQuery = "SELECT club_id FROM club_player WHERE player_id = ?::uuid";
        String insertClubPlayerQuery = """
        INSERT INTO club_player (id, club_id, player_id)
        VALUES (?::uuid, ?::uuid, ?::uuid)
    """;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (
                PreparedStatement checkExistStmt = connection.prepareStatement(checkPlayerExistQuery);
                PreparedStatement insertPlayerStmt = connection.prepareStatement(insertPlayerQuery);
                PreparedStatement checkClubStmt = connection.prepareStatement(checkIfPlayerInClubQuery);
                PreparedStatement insertClubStmt = connection.prepareStatement(insertClubPlayerQuery)
            ) {
                for (PlayerDTO player : players) {
                    UUID playerId = UUID.fromString(player.getId());

                    // Vérifie si le joueur existe
                    boolean playerExists = false;
                    checkExistStmt.setObject(1, playerId);
                    try (ResultSet rs = checkExistStmt.executeQuery()) {
                        if (rs.next()) {
                            playerExists = true;
                        }
                    }

                    // Si le joueur n'existe pas, on le crée
                    if (!playerExists) {
                        insertPlayerStmt.setObject(1, playerId);
                        insertPlayerStmt.setString(2, player.getName());
                        insertPlayerStmt.setInt(3, player.getNumber());
                        insertPlayerStmt.setString(4, player.getPosition().toString());
                        insertPlayerStmt.setString(5, player.getNationality().toString());
                        insertPlayerStmt.setInt(6, player.getAge());
                        insertPlayerStmt.executeUpdate();
                    }

                    // Vérifie si le joueur est déjà dans un club
                    checkClubStmt.setObject(1, playerId);
                    try (ResultSet rs = checkClubStmt.executeQuery()) {
                        if (rs.next()) {
                            UUID existingClubId = UUID.fromString(rs.getString("club_id"));
                            if (!existingClubId.equals(clubId)) {
                                connection.rollback();
                                throw new IllegalArgumentException("Le joueur " + player.getName() + " est déjà dans un autre club.");
                            }
                        } else {
                            // Joueur non lié, on l'ajoute au club
                            insertClubStmt.setObject(1, UUID.randomUUID());
                            insertClubStmt.setObject(2, clubId);
                            insertClubStmt.setObject(3, playerId);
                            insertClubStmt.executeUpdate();
                        }
                    }
                }

                connection.commit();
                return findPlayersByClubId(clubId);
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Erreur lors de l'ajout des joueurs au club", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public List<PlayerDTO> findPlayersByClubId(UUID clubId) {
        String query = """
        SELECT p.id, p.name, p.age, p.number, p.player_position, p.nationality
        FROM players p
        JOIN club_player cp ON cp.player_id = p.id
        WHERE cp.club_id = ?::uuid
    """;

        List<PlayerDTO> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setObject(1, clubId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlayerDTO player = new PlayerDTO();
                    player.setId(rs.getString("id"));
                    player.setName(rs.getString("name"));
                    player.setAge(rs.getInt("age"));
                    player.setNumber(rs.getInt("number"));
                    player.setPosition(Position.valueOf(rs.getString("player_position")));
                    player.setNationality(Nationality.valueOf(rs.getString("nationality")));
                    result.add(player);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des joueurs du club", e);
        }

        return result;
    }

    public List<ClubStatisticDTO> findBySeasonYear(int seasonYear, boolean hasToBeClassified) {
        String query = """
        SELECT cs.rankingpoints, cs.scoredgoals, cs.concededgoals, cs.differencegoals, cs.cleansheetnumber,
               c.id as club_id, c.name as club_name, c.acronym, c.year_creation, c.stadium,
               co.id AS coach_id, co.name as coach_name, co.nationality as coach_nationality
        FROM club_statistic cs
        JOIN club c ON cs.club_id = c.id
        JOIN coach co ON c.coach_id = co.id
        WHERE cs.season_year = ?
        ORDER BY
            CASE WHEN ? THEN cs.rankingpoints END DESC,
            CASE WHEN ? THEN cs.differencegoals END DESC,
            CASE WHEN ? THEN cs.cleansheetnumber END DESC,
            c.name ASC
    """;

        List<ClubStatisticDTO> result = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setInt(1, seasonYear);
            for (int i = 2; i <= 4; i++) {
                statement.setBoolean(i, hasToBeClassified);
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Coach coach = new Coach(
                        rs.getString("coach_id"),
                        rs.getString("coach_name"),
                        Nationality.valueOf(rs.getString("coach_nationality"))
                    );

                    Club club = new Club(
                        rs.getString("club_id"),
                        rs.getString("club_name"),
                        rs.getString("acronym"),
                        rs.getInt("year_creation"),
                        rs.getString("stadium"),
                        coach
                    );

                    ClubStatisticDTO stat = new ClubStatisticDTO(
                        club,
                        rs.getInt("rankingpoints"),
                        rs.getInt("scoredgoals"),
                        rs.getInt("concededgoals"),
                        rs.getInt("differencegoals"),
                        rs.getInt("cleansheetnumber")
                    );

                    result.add(stat);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des statistiques", e);
        }

        return result;
    }

}
