package com.rose.estoque.produto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarProdutoRequest(
        @NotBlank String nome,
        @NotNull @Min(0) Integer quantidade) {
}