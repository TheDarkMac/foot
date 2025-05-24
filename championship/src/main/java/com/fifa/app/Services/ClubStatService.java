package com.fifa.app.Services;

import com.fifa.app.DAO.ClubDAO;
import com.fifa.app.DTO.ClubStatDTO;
import com.fifa.app.DTO.ClubStatisticDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubStatService {

    private final ClubDAO statisticDAO;

    public  List<ClubStatisticDTO> findBySeasonYear(int seasonYear, boolean hasToBeClassified){
        return statisticDAO.findBySeasonYear(seasonYear, hasToBeClassified);
    }
}
