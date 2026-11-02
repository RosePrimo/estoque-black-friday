package com.rose.estoque.produto;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoConsumer {

    private final ProdutoService service;

    public PedidoConsumer(ProdutoService service) {
        this.service = service;
    }

    @KafkaListener(topics = ProdutoService.TOPICO_PEDIDOS, groupId = "estoque")
    public void consumir(ConsumerRecord<String, String> pedido) {
        service.confirmarCompra(Long.valueOf(pedido.key()), Integer.parseInt(pedido.value()));
    }
}