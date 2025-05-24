package com.fifa.analytics.RestController;

import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.Player;
import com.fifa.analytics.Enum.DurationUnit;
import com.fifa.analytics.Services.ChampionshipService;
import com.fifa.analytics.Services.ClubService;
import com.fifa.analytics.Services.PlayerService;
import com.fifa.analytics.Services.SynchronizationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/")
public class FifaController {

    private final ClubService clubService;
    private final ChampionshipService championshipService;
    private SynchronizationService synchronizationService;

    private PlayerService playerService;

    @GetMapping
    public String index() {
        return "Fifa Analytics";
    }

    @PostMapping("synchronization")
    public ResponseEntity<String> synchronisation(){
        synchronizationService.synchronize().block();
        return ResponseEntity.ok("Synchronization completed");
    }

    @GetMapping("bestPlayers")
    public ResponseEntity<List<Player>> bestPlayer(
            @RequestParam(defaultValue = "100") Integer top,
            @RequestParam(defaultValue = "MINUTE") DurationUnit playingTimeUnit,
            @RequestParam(defaultValue = "2023") Integer seasonYear){
        return ResponseEntity.ok(playerService.getBestPlayers(top,playingTimeUnit,seasonYear));
    }

    @GetMapping("bestClubs")
    public ResponseEntity<List<Club>> bestClub(@RequestParam(defaultValue = "100") Integer top,@RequestParam(defaultValue = "2023") Integer seasonYear){
        return ResponseEntity.ok(clubService.getBestClubs(top,seasonYear));
    }

    @GetMapping("championshipRankings")
    public ResponseEntity<Object> championshipRanking(@RequestParam(defaultValue = "10") Integer top, @RequestParam(defaultValue = "2023") Integer season){
        return ResponseEntity.ok(championshipService.getChampionshipRanks(season,top));
    }
}
