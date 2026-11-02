package com.rose.estoque.produto;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic topicoPedidos() {
        return TopicBuilder.name(ProdutoService.TOPICO_PEDIDOS).partitions(2).replicas(1).build();
    }
}