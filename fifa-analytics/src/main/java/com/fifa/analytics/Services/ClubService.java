package com.fifa.analytics.Services;

import com.fifa.analytics.Configuration.ChampionshipClient;
import com.fifa.analytics.DAO.ClubDAO;
import com.fifa.analytics.DAO.ClubStatDAO;
import com.fifa.analytics.DAO.PlayerDAO;
import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.ClubStat;
import com.fifa.analytics.RestModels.ClubRest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Data
public class ClubService {

    private final ClubDAO clubDAO;
    private ChampionshipClient championshipClient;
    private PlayerDAO playerDAO;
    private ClubStatDAO clubStatDAO;

    public Flux<ClubRest> getClubs(String championship) {
        return championshipClient.getWebClient()
                .get()
                .uri("/{championship}/clubs",championship)
                .retrieve()
                .bodyToFlux(ClubRest.class);
    }

    public Mono<List<Club>> getAllClubs() {
        return Mono.fromCallable(clubDAO::getAll).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ClubRest> getClubStatistics(String championship,Integer season){
        return championshipClient.getWebClient()
                .get()
                .uri("/{championship}/clubs/statistics/{season}",championship,season)
                .retrieve()
                .bodyToFlux(ClubRest.class)
                .doOnNext(clubRest -> {clubRest.setSeason(season);})
                .doOnNext(clubRest -> clubRest.setChampionshipName(championship));
    }

    public Mono<List<Club>> saveAll(List<Club> clubs) {
        return Mono.fromCallable(()->clubDAO.saveAll(clubs)).subscribeOn(Schedulers.boundedElastic());
    }

    public List<Club> getBestClubs(Integer top, Integer seasonYear) {
        return clubDAO.getAll().stream()
                .filter(club -> club.getClubStats() != null)
                .map(club -> {
                    ClubStat seasonStat = club.getClubStats().stream()
                            .filter(stat -> stat.getSeason() != null && seasonYear.equals(stat.getSeason().getYear()))
                            .findFirst()
                            .orElse(null);
                    assert seasonStat != null;
                    club.setClubStats(List.of(seasonStat));
                    return Map.entry(club, seasonStat);
                })
                .filter(entry -> entry.getValue() != null) // Exclure les clubs sans stats pour cette saison
                .sorted(
                        Comparator.comparing((Map.Entry<Club, ClubStat> e) -> e.getValue().getRankingPoints(),
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(e -> e.getValue().getDifferenceGoals(),
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(e -> e.getValue().getScoredGoals(),
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(e -> e.getValue().getCleanSheetNumber(),
                                        Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .limit(top)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Mono<List<ClubStat>> saveAllStats(List<ClubStat> clubs) {
       return Mono.fromCallable(()->clubStatDAO.saveAll(clubs));
    }

    private ClubStat getStatForYear(Club club, Integer seasonYear) {
        return club.getClubStats().stream()
                .filter(stat -> stat.getSeason() != null && seasonYear.equals(stat.getSeason().getYear()))
                .findFirst()
                .orElse(null);
    }

}
