package com.fifa.app.RestControllers;

import com.fifa.app.DAO.MatchGoalDAO;
import com.fifa.app.DTO.MatchDisplayDTO;
import com.fifa.app.Entities.GoalRequest;
import com.fifa.app.Entities.Match;
import com.fifa.app.Entities.MatchStatusUpdateRequest;
import com.fifa.app.Services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchGoalDAO matchGoalDAO;

    @PostMapping("/matchMaker/{seasonYear}")
    public ResponseEntity<?> generateMatches(@PathVariable int seasonYear) {
        try {
            List<MatchDisplayDTO> matches = matchService.generateSeasonMatches(seasonYear);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/matches/{seasonYear}")
    public ResponseEntity<List<MatchDisplayDTO>> getMatchesBySeason(
        @PathVariable int seasonYear,
        @RequestParam(required = false) String matchStatus,
        @RequestParam(required = false) String clubPlayingName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime matchAfter,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime matchBeforeOrEquals
    ) {
        List<MatchDisplayDTO> matches = matchService.getMatchesFiltered(seasonYear, matchStatus, clubPlayingName, matchAfter, matchBeforeOrEquals);
        if (matches.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(matches);
        }
        return ResponseEntity.ok(matches);
    }

    @PutMapping("/matches/{matchId}/status")
    public ResponseEntity<?> updateMatchStatus(
        @PathVariable UUID matchId,
        @RequestBody MatchStatusUpdateRequest request
    ) {
        try {
            MatchDisplayDTO updatedMatch = matchService.updateMatchStatus(matchId, request.getStatus());
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @PostMapping("/matches/{id}/goals")
    public ResponseEntity<?> addGoalsToMatch(
        @PathVariable String id,
        @RequestBody List<GoalRequest> goalRequests) {
        try {
            MatchDisplayDTO updatedMatch = matchGoalDAO.addGoalsToMatch(id, goalRequests);
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding goals to match");
        }
    }
}
