package com.fifa.app.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransfertDTO {
    private UUID id;
    private LocalDate transfertDate;
    private String transfertType;
    private PlayerDTO player;
}
