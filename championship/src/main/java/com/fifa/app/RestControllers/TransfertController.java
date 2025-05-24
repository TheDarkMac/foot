package com.fifa.app.RestControllers;

import com.fifa.app.DTO.TransfertDTO;
import com.fifa.app.Services.TransfertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transfert")
@RequiredArgsConstructor
public class TransfertController {

    private final TransfertService transfertService;

    @GetMapping
    public ResponseEntity<List<TransfertDTO>> getAll() {
        List<TransfertDTO> transferts = transfertService.getAll();
        return ResponseEntity.ok(transferts);
    }
}
