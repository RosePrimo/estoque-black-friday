package com.rose.estoque.produto;

import jakarta.persistence.*;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer quantidade;

    protected Produto() {
    }

    public Produto(String nome, Integer quantidade) {
        this.nome = nome;
        this.quantidade = quantidade;
    }

    public void baixarEstoque(int quantidadeComprada) {
        if (quantidadeComprada > this.quantidade) {
            throw new IllegalStateException("Estoque insuficiente");
        }
        this.quantidade = this.quantidade - quantidadeComprada;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Integer getQuantidade() { return quantidade; }
}