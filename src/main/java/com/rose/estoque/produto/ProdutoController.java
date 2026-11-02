package com.rose.estoque.produto;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody CriarProdutoRequest request) {
        return service.criar(request);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/{id}/comprar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompraAceitaResponse comprar(@PathVariable Long id, @Valid @RequestBody ComprarRequest request) {
        return service.comprar(id, request);
    }
}