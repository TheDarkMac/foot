package com.fifa.analytics.Services;

import com.fifa.analytics.DTO.ChampionshipRank;
import com.fifa.analytics.DTO.Club;
import com.fifa.analytics.DTO.ClubStat;
import com.fifa.analytics.Enum.Championship;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ChampionshipService {

    private final ClubService clubService;

    public List<ChampionshipRank> getChampionshipRanks(Integer season, Integer limit) {
        return Arrays.stream(Championship.values())
                .map(championship -> {
                    List<Club> clubs = clubService.getAllClubs().block();
                    if (clubs == null || clubs.isEmpty()) {
                        return new ChampionshipRank(championship.toString(), 0);
                    }
                    List<Integer> differences = clubs.stream()
                            .filter(club -> club.getChampionship()==championship)
                            .map(Club::getClubStats)
                            .filter(Objects::nonNull)
                            .flatMap(List::stream)
                            .filter(clubStat -> Objects.equals(clubStat.getSeason().getYear(), season))
                            .map(ClubStat::getDifferenceGoals)
                            .filter(Objects::nonNull)
                            .sorted()
                            .toList();
                    System.out.println(differences);
                    if (differences.isEmpty()) {
                        return new ChampionshipRank(championship.toString(), 0);
                    }
                    double median = calculateMedian(differences);
                    return new ChampionshipRank(championship.toString(), median);
                })
                .sorted(Comparator.comparingDouble(ChampionshipRank::getMedian).reversed())
                .limit(limit == null ? Long.MAX_VALUE : limit)
                .collect(Collectors.toList());
    }

    private double calculateMedian(List<Integer> sortedValues) {
        int size = sortedValues.size();
        if (size == 0) return 0;

        int middle = size / 2;
        return size % 2 == 1
                ? sortedValues.get(middle)
                : (sortedValues.get(middle - 1) + sortedValues.get(middle)) / 2.0;
    }
}