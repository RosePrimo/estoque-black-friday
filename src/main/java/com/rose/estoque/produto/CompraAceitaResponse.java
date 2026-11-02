package com.rose.estoque.produto;

public record CompraAceitaResponse(Long produtoId, Integer quantidade, String status) {
}