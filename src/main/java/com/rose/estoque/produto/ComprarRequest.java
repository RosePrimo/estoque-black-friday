package com.rose.estoque.produto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ComprarRequest(@NotNull @Min(1) Integer quantidade) {
}