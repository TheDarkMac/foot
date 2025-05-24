package com.fifa.app.Entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfert {
    private UUID id;
    private UUID playerId;
    private LocalDate transfertDate;
    private String transfertType;
}
