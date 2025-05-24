package com.fifa.app.DAO;

import com.fifa.app.Entities.Season;
import com.fifa.app.Entities.SeasonStatus;
import com.fifa.app.Entities.SeasonStatusUpdateRequest;
import com.fifa.app.Mapper.SeasonMapper;
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
public class SeasonDAO {

    private final DataSource dataSource;

    public List<Season> findAll() {
        List<Season> seasonList = new ArrayList<>();
        String query = "SELECT * FROM season";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                Season season = SeasonMapper.mapFromResultSet(resultSet);
                seasonList.add(season);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des saisons", e);
        }

        return seasonList;
    }

    public List<Season> createSeasons(List<Season> seasons) {
        List<Season> createdSeasons = new ArrayList<>();

        String insertQuery = """
            INSERT INTO season (id, alias, status, year)
            VALUES (?::uuid, ?, ?::season_status, ?)
        """;

        try (Connection connection = dataSource.getConnection()) {
            for (Season season : seasons) {
                String generatedId = UUID.randomUUID().toString();
                String alias = season.getAlias();
                int year = season.getYear();
                SeasonStatus status = SeasonStatus.NOT_STARTED;

                try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                    stmt.setObject(1, generatedId);
                    stmt.setString(2, alias);
                    stmt.setString(3, status.name());
                    stmt.setInt(4, year);
                    stmt.executeUpdate();
                }

                createdSeasons.add(new Season(generatedId, year, alias, status));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création des saisons", e);
        }

        return createdSeasons;
    }

    public Season updateSeasonStatus(int seasonYear, SeasonStatus newStatus) {
        String selectQuery = "SELECT * FROM season WHERE year = ?";
        String updateQuery = "UPDATE season SET status = ?::season_status WHERE id = ?::uuid";

        try (Connection connection = dataSource.getConnection()) {
            Season existingSeason;

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, seasonYear);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        existingSeason = SeasonMapper.mapFromResultSet(rs);
                    } else {
                        throw new RuntimeException("Saison non trouvée pour l’année " + seasonYear);
                    }
                }
            }

            if (!SeasonMapper.isValidStatusTransition(existingSeason.getStatus(), newStatus)) {
                throw new IllegalArgumentException("Changement de statut invalide : " +
                    existingSeason.getStatus() + " → " + newStatus);
            }

            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setString(1, newStatus.name());
                updateStmt.setObject(2, existingSeason.getId());
                updateStmt.executeUpdate();
            }

            existingSeason.setStatus(newStatus);
            return existingSeason;

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du statut de la saison", e);
        }
    }
}
