package com.fifa.analytics.Services;

import com.fifa.analytics.Configuration.ChampionshipClient;
import com.fifa.analytics.DAO.PlayerDAO;
import com.fifa.analytics.DTO.Player;
import com.fifa.analytics.DTO.PlayerStatistics;
import com.fifa.analytics.Enum.DurationUnit;
import com.fifa.analytics.Enum.Position;
import com.fifa.analytics.RestModels.PlayerRest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PlayerService {

    private ChampionshipClient championshipClient;
    private PlayerDAO playerDAO;

    public Flux<PlayerRest> getPlayers(String championship) {
        return championshipClient.getWebClient()
                .get()
                .uri("/{championship}/players",championship)
                .retrieve()
                .onStatus(status -> status.value() == 404, response -> {
                    System.out.println("Aucun endpoint pour " + championship + " (404 ignoré).");
                    return Mono.empty(); // <- retourne un Mono vide pour ne pas lever d'exception
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Erreur HTTP " + response.statusCode() + ": " + body)))
                )
                .bodyToFlux(PlayerRest.class)
                .onErrorResume(e -> {
                    System.out.println("Erreur lors de l'appel pour " + championship + " : " + e.getMessage());
                    return Flux.empty();
                });
    }

    public List<Player> getBestPlayers(Integer top, DurationUnit playingTimeUnit, Integer seasonYear) {
        return playerDAO.getAllPlayers().stream()
                .filter(player -> player.getPlayerStatistics()
                        .stream()
                        .filter(playerStatistics -> Objects.equals(playerStatistics.getSeason(), seasonYear))
                        .isParallel())
                .filter(player ->
                        player.getPlayerStatistics() != null &&
                                !player.getPlayerStatistics().isEmpty() &&
                            player.getPosition() != Position.GOAL_KEEPER // to prevent the random value where a GoalKeeper is a superhero when I simulate the data fetching
                        )
                .map(player -> {
                    PlayerStatistics stats = player.getPlayerStatistics()
                            .stream()
                            .peek(playerStatistics -> {
                                DecimalFormat df = new DecimalFormat();
                                playerStatistics.getPlayingTime().setDurationUnit(playingTimeUnit);
                                Double time = playerStatistics.getPlayingTime().getValue();
                                if(playingTimeUnit == DurationUnit.MINUTE){
                                    time =  time / 60;
                                }
                                if(playingTimeUnit == DurationUnit.HOUR){
                                    time =  time / 3600;
                                }
                                Double t = new BigDecimal(String.valueOf(time))
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .doubleValue();

                                playerStatistics.getPlayingTime().setValue(t);
                            })
                            .findFirst()
                            .orElse(null);
                    return Map.entry(player, stats);
                })
                .sorted(
                        Comparator.comparingDouble((Map.Entry<Player, PlayerStatistics> e) -> e.getValue().getScoredGoals()).reversed()
                                .thenComparingDouble(e -> e.getValue().getPlayingTime().getValue())
                )
                .limit(top)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public Mono<List<Player>> saveAll(List<Player> players) {
        return Mono.fromCallable(()->playerDAO.saveAll(players)).subscribeOn(Schedulers.boundedElastic());
    }
}
