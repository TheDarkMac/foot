package com.fifa.analytics.Services;

import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.Player;
import com.fifa.analytics.DTO.Season;
import com.fifa.analytics.Enum.Championship;
import com.fifa.analytics.Mapper.RestToModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class SynchronizationService {

    private final PlayerService playerService;
    private final PlayerStatisticsService playerStatisticsService;
    private final ClubService clubService;
    private final SeasonService seasonService;

    public Mono<Void> synchronize() {
        return Flux.fromArray(Championship.values())
                .flatMap(this::processChampionship)
                .then();
    }

    private Mono<Void> processChampionship(Championship championship) {
        return seasonService.getSeasons(championship.name())
                .collectList()
                .flatMap(seasons -> seasonService.saveAll(seasons)
                        .thenMany(Flux.fromIterable(seasons))
                        .flatMap(season -> processSeason(championship, season))
                        .then()
                )
                .then(processPlayers(championship));
    }

    private Mono<Void> processSeason(Championship championship, Season season) {
        return clubService.getClubStatistics(championship.name(), season.getYear())
                .map(RestToModel::mapToClub)
                .collectList()
                .flatMap(clubs -> clubService.saveAll(clubs)
                        .thenMany(Flux.fromIterable(clubs))
                        .flatMap(club -> processClubStats(club, season))
                        .then());
    }

    private Mono<Void> processClubStats(Club club, Season season) {
        if (club.getClubStats() == null || club.getClubStats().isEmpty()) {
            return Mono.empty();
        }

        club.getClubStats().forEach(stat -> stat.setSeason(season));
        return clubService.saveAllStats(club.getClubStats()).then();
    }

    private Mono<Void> processPlayers(Championship championship) {
        return playerService.getPlayers(championship.name())
                .map(RestToModel::mapToPlayer)
                .collectList()
                .flatMap(players -> playerService.saveAll(players)
                        .thenMany(Flux.fromIterable(players))
                        .flatMap(player -> processPlayerStatistics(championship, player))
                        .then());
    }

    private Mono<Void> processPlayerStatistics(Championship championship, Player player) {
        return seasonService.getSeasons(championship.name())
                .next()
                .flatMapMany(season -> playerStatisticsService.getPlayerStatistics(
                        championship.name(),
                        player.getId(),
                        season.getYear()
                ))
                .collectList()
                .flatMap(playerStatisticsService::saveAll)
                .then();
    }
}