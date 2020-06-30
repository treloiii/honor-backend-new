package com.trelloiii.honor.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.trelloiii.honor.model.Ordens;
import com.trelloiii.honor.model.Veterans;
import com.trelloiii.honor.services.OrdenService;
import com.trelloiii.honor.view.Views;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ordens")
public class OrdensController {
    private final OrdenService ordenService;

    public OrdensController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    @JsonView(Views.ImportantView.class)
    public List<Ordens> getAllOrdens() {
        return ordenService.getAllOrdens();
    }

    @GetMapping("/{id}")
    @JsonView(Views.FullView.class)
    public Ordens getOrden(@PathVariable Long id) {
        return ordenService.findById(id);
    }

    @PostMapping
    public ResponseEntity<?> addOrden(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String shortDescription,
            @RequestParam MultipartFile titleImage
    ) {
        try {
            return ResponseEntity.ok(ordenService.addOrden(name, description, shortDescription, titleImage));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{id}")
    public Ordens updateOrden(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String shortDescription,
            @RequestParam(required = false) MultipartFile titleImage,
            @PathVariable Long id
    ) {
        return ordenService.updateOrden(name, description, shortDescription, titleImage, id);
    }

    @DeleteMapping("{id}")
    public void deleteOrden(@PathVariable Long id) {
        ordenService.deleteOrden(id);
    }

    @PostMapping("/veterans")
    public Veterans addVeteran(@RequestParam Long id,
                               @RequestParam String fio,
                               @RequestParam String post,
                               @RequestParam String rank) {
        return ordenService.addVeteran(id,fio,post,rank);
    }
}
