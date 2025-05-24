package com.fifa.app.Services;


import com.fifa.app.DAO.SeasonDAO;
import com.fifa.app.Entities.Season;
import com.fifa.app.Entities.SeasonStatus;
import com.fifa.app.Entities.SeasonStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private final SeasonDAO seasonDAO;

    public List<Season> getAll() {
        return seasonDAO.findAll();
    }

    public List<Season> createSeason(List<Season> seasons) {
        return seasonDAO.createSeasons(seasons);
    }
    public Season updateSeasonStatus(int seasonYear , SeasonStatus status) {
        return seasonDAO.updateSeasonStatus(seasonYear , status);
    }

}
