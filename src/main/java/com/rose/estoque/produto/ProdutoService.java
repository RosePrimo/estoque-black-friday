package com.rose.estoque.produto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoService {

    public static final String TOPICO_PEDIDOS = "pedidos";
    private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository repository;
    private final EstoqueRedis estoque;
    private final KafkaTemplate<String, String> kafka;

    public ProdutoService(ProdutoRepository repository, EstoqueRedis estoque,
                          KafkaTemplate<String, String> kafka) {
        this.repository = repository;
        this.estoque = estoque;
        this.kafka = kafka;
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

    /** Responde na hora: o Redis reserva e o Kafka leva o pedido para ser gravado depois. */
    public CompraAceitaResponse comprar(Long id, ComprarRequest request) {
        if (!estoque.existe(id)) {
            estoque.inicializar(id, buscarProduto(id).getQuantidade());
        }
        if (!estoque.reservar(id, request.quantidade())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente");
        }
        // a chave é o id do produto: pedidos do mesmo produto caem na mesma partição, em ordem
        kafka.send(TOPICO_PEDIDOS, id.toString(), request.quantidade().toString());
        return new CompraAceitaResponse(id, request.quantidade(), "PROCESSANDO");
    }

    /** Chamado pelo consumidor do Kafka, em segundo plano. */
    @Transactional
    public void confirmarCompra(Long id, int quantidade) {
        int linhas = repository.baixarEstoque(id, quantidade);
        if (linhas == 0) {
            log.error("Pedido sem estoque no banco: produto={} quantidade={}", id, quantidade);
            return;
        }
        log.info("Pedido confirmado: produto={} quantidade={}", id, quantidade);
    }

    private Produto buscarProduto(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }
}