package com.fifa.app.Services;

import com.fifa.app.DAO.PlayerDAO;
import com.fifa.app.DTO.PlayerDTO;
import com.fifa.app.Entities.Player;
import com.fifa.app.Entities.PlayerCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerDAO playerDAO;

    public List<Player> findAll(PlayerCriteria criteria) {
        return playerDAO.findAll(criteria);
    }


    public List<Player> createOrUpdatePlayers(List<Player> players) {
        return playerDAO.createOrUpdatePlayers(players);
    }

    public List<PlayerDTO> findPlayersByClubId(String clubId) {
        return playerDAO.findPlayersByClubId(clubId);
    }
}
