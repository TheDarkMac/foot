package com.fifa.app.DTO;

import com.fifa.app.Entities.Club;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClubStatisticDTO {
    private Club club;
    private int rankingPoints;
    private int scoredGoals;
    private int concededGoals ;
    private int differenceGoals ;
    private int cleanSheetNumber;
}
