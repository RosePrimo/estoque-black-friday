package com.rose.estoque.produto;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;
    private final EstoqueRedis estoque;

    public ProdutoService(ProdutoRepository repository, EstoqueRedis estoque) {
        this.repository = repository;
        this.estoque = estoque;
    }

    @Transactional
    public ProdutoResponse criar(CriarProdutoRequest request) {
        Produto produto = repository.save(new Produto(request.nome(), request.quantidade()));
        estoque.inicializar(produto.getId(), produto.getQuantidade());
        return ProdutoResponse.de(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return ProdutoResponse.de(buscarProduto(id));
    }

    @Transactional
    public ProdutoResponse comprar(Long id, ComprarRequest request) {
        // 1. Produto antigo, que ainda não está no Redis: carrega do banco uma vez.
        if (!estoque.existe(id)) {
            estoque.inicializar(id, buscarProduto(id).getQuantidade());
        }

        // 2. Porteiro: o Redis decide quem conseguiu uma unidade.
        if (!estoque.reservar(id, request.quantidade())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente");
        }

        // 3. Só quem reservou chega ao banco. O UPDATE atômico continua como rede de segurança.
        int linhasAlteradas = repository.baixarEstoque(id, request.quantidade());
        if (linhasAlteradas == 0) {
            estoque.devolver(id, request.quantidade());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente");
        }
        return ProdutoResponse.de(buscarProduto(id));
    }

    private Produto buscarProduto(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }
}