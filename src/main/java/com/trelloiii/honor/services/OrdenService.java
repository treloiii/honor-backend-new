package com.trelloiii.honor.services;

import com.trelloiii.honor.exceptions.EntityNotFoundException;
import com.trelloiii.honor.model.Ordens;
import com.trelloiii.honor.repository.OrdenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdenService {
    private final OrdenRepository ordenRepository;

    public OrdenService(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    public List<Ordens> getAllOrdens(){
        return ordenRepository.findAll();
    }
    public Ordens findById(Long id){
        return ordenRepository.findById(id).orElseThrow(()->
                new EntityNotFoundException(String.format("Orden with id = %d not found",id))
        );
    }
}
