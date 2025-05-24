package com.fifa.app.RestControllers;

import com.fifa.app.DTO.ClubStatDTO;
import com.fifa.app.DTO.ClubStatisticDTO;
import com.fifa.app.Services.ClubStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubStatController {

    private final ClubStatService service;

    @GetMapping("/statistics/{seasonYear}")
    public ResponseEntity<List<ClubStatisticDTO>> getStatistics(
        @PathVariable int seasonYear,
        @RequestParam(defaultValue = "false") boolean hasToBeClassified
    ) {
        List<ClubStatisticDTO> stats = service.findBySeasonYear(seasonYear, hasToBeClassified);
        return ResponseEntity.ok(stats);
    }

}
