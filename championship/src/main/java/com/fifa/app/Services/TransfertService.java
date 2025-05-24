package com.fifa.app.Services;

import com.fifa.app.DAO.TransfertDAO;
import com.fifa.app.DTO.TransfertDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransfertService {

    private final TransfertDAO transfertDAO;

    public List<TransfertDTO> getAll() {
        return transfertDAO.getAll();
    }
}
