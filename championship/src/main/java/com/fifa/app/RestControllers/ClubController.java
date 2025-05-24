package com.fifa.app.RestControllers;

import com.fifa.app.DTO.PlayerDTO;
import com.fifa.app.Entities.Club;
import com.fifa.app.Entities.Player;
import com.fifa.app.Services.ClubService;
import com.fifa.app.Services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService service;
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<Club>> getAll() {
        List<Club> clubs = service.findAll();
        return ResponseEntity.ok(clubs);
    }
    @PutMapping
    public ResponseEntity<List<Club>> createOrUpdateClubs(@RequestBody List<Club> clubs) {
        service.createOrUpdateClubs(clubs);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/{id}/players")
    public ResponseEntity<List<PlayerDTO>> findPlayersByClubId(@PathVariable String id) {
        List<PlayerDTO> players = playerService.findPlayersByClubId(id);
        if (players.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(players);
    }

    @PutMapping("/{id}/players")
    public ResponseEntity<List<PlayerDTO>> createOrUpdateClubPlayers(@PathVariable UUID id,@RequestBody List<PlayerDTO> players) {
        service.updateClubPlayers(id , players);
        return ResponseEntity.ok(players);
    }

    @PostMapping("/{id}/players")
    public ResponseEntity<List<PlayerDTO>> addPlayersToClub(
        @PathVariable("id") UUID clubId,
        @RequestBody List<PlayerDTO> players
    ) {
        service.addPlayersToClub(clubId, players);
        return ResponseEntity.ok(players);
    }


}
