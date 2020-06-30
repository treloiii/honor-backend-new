package com.trelloiii.honor.controllers;

import com.trelloiii.honor.model.Ordens;
import com.trelloiii.honor.services.OrdenService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/ordens")
public class OrdensController {
    private final OrdenService ordenService;

    public OrdensController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public List<Ordens> getAllOrdens(){
        return ordenService.getAllOrdens();
    }
    @GetMapping("/{id}")
    public Ordens getOrden(@PathVariable Long id){
        return ordenService.findById(id);
    }
    @PostMapping
    public Ordens addOrden(@RequestParam String name,
                           @RequestParam String description,
                           @RequestParam String shortDescription,
                           @RequestParam MultipartFile titleImage){
        return null;
    }
}
