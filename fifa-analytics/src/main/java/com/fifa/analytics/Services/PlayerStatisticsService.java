package com.fifa.analytics.Services;

import com.fifa.analytics.Configuration.ChampionshipClient;
import com.fifa.analytics.DAO.PlayerDAO;
import com.fifa.analytics.DAO.PlayerStatisticsDAO;
import com.fifa.analytics.DTO.PlayerStatistics;
import com.fifa.analytics.Mapper.RestToModel;
import com.fifa.analytics.RestModels.PlayerStatisticsRest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@AllArgsConstructor
@Data
@Service
public class PlayerStatisticsService {

    private ChampionshipClient championshipClient;
    private PlayerStatisticsDAO playerStatisticsDAO;
    private PlayerDAO playerDAO;

    public Mono<PlayerStatistics> getPlayerStatistics(String championship, String playerId, Integer season) {
        return championshipClient.getWebClient()
                .get()
                .uri("{championship}/players/{playerId}/statistics/{season}", championship, playerId, season)
                .retrieve()
                .onStatus(status -> status.value() == 404, response -> {
                    System.out.println("Aucun endpoint pour " + championship + " (404 ignoré).");
                    return Mono.empty();  // Retourne Mono vide sans lever d'exception
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Erreur HTTP " + response.statusCode() + ": " + body)))
                )
                .bodyToMono(PlayerStatisticsRest.class)
                .doOnNext(playerStatisticsRest -> {
                    playerStatisticsRest.setPlayerId(playerId);
                    playerStatisticsRest.setSeasonYear(season);
                })
                .map(RestToModel::mapToPlayerStatistics)
                .onErrorResume(e -> {
                    System.out.println("Erreur lors de l'appel pour " + championship + " : inside statics " + e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<List<PlayerStatistics>> saveAll(List<PlayerStatistics> playerStatistics) {
        return Mono.fromCallable(()->playerStatisticsDAO.saveAll(playerStatistics))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
