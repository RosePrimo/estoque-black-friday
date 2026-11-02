package com.rose.estoque.produto;

public record ProdutoResponse(Long id, String nome, Integer quantidade) {

    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getQuantidade());
    }
}