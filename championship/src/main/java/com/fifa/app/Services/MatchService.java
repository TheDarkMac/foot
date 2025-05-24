package com.fifa.app.Services;

import com.fifa.app.DAO.MatchDAO;
import com.fifa.app.DTO.MatchDisplayDTO;
import com.fifa.app.Entities.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchDAO matchDAO;

    public List<MatchDisplayDTO> generateSeasonMatches(int seasonYear) {
        return matchDAO.createMatchesForSeason(seasonYear);
    }

    public List<MatchDisplayDTO> getMatchesFiltered(int seasonYear, String matchStatus, String clubPlayingName,
                                                    LocalDateTime matchAfter, LocalDateTime matchBeforeOrEquals) {
        return matchDAO.findMatchesFiltered(seasonYear, matchStatus, clubPlayingName, matchAfter, matchBeforeOrEquals);
    }

    public MatchDisplayDTO updateMatchStatus(UUID matchId, String newStatusStr){
        return matchDAO.updateMatchStatus(matchId, newStatusStr);
    }

}
