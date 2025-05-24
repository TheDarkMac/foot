package com.fifa.app.DAO;

import com.fifa.app.DTO.PlayerDTO;
import com.fifa.app.DTO.TransfertDTO;
import com.fifa.app.Entities.Nationality;
import com.fifa.app.Entities.Position;
import com.fifa.app.Entities.Transfert;
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
public class TransfertDAO {

    private final DataSource dataSource;

    public List<TransfertDTO> getAll() {
        List<TransfertDTO> transfertList = new ArrayList<>();

        String query = """
        SELECT
            t.id AS transfert_id,
            t.transfert_date,
            t.transfert_type,
            p.id AS player_id,
            p.name,
            p.age,
            p.number,
            p.player_position,
            p.nationality
        FROM transfert t
        JOIN players p ON t.player_id = p.id
        ORDER BY t.transfert_date DESC
    """;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)
        ) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                PlayerDTO player = new PlayerDTO(
                    rs.getString("player_id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getInt("number"),
                    Position.valueOf(rs.getString("player_position")),
                    Nationality.valueOf(rs.getString("nationality"))
                );

                TransfertDTO transfert = new TransfertDTO(
                    UUID.fromString(rs.getString("transfert_id")),
                    rs.getDate("transfert_date").toLocalDate(),
                    rs.getString("transfert_type"),
                    player
                );

                transfertList.add(transfert);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des transferts", e);
        }

        return transfertList;
    }

}
