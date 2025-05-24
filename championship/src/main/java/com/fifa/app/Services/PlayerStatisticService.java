package com.fifa.app.Services;

import com.fifa.app.DAO.PlayerStatisticDAO;
import com.fifa.app.DTO.PlayerStatisticDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerStatisticService {

    private final PlayerStatisticDAO statisticDAO;

    public PlayerStatisticDTO getStatisticByPlayerAndYear(String playerId, int seasonYear) {
        return statisticDAO.findByPlayerIdAndSeasonYear(playerId, seasonYear);
    }
}
