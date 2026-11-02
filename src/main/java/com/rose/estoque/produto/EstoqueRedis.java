package com.rose.estoque.produto;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EstoqueRedis {

    private final StringRedisTemplate redis;

    public EstoqueRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String chave(Long id) {
        return "estoque:" + id;
    }

    /** Coloca o estoque no Redis só se ainda não estiver lá. */
    public void inicializar(Long id, int quantidade) {
        redis.opsForValue().setIfAbsent(chave(id), String.valueOf(quantidade));
    }

    /** Tenta reservar. O DECR do Redis é atômico: dois clientes nunca pegam a mesma unidade. */
    public boolean reservar(Long id, int quantidade) {
        Long restante = redis.opsForValue().decrement(chave(id), quantidade);
        if (restante == null || restante < 0) {
            devolver(id, quantidade); // passou do zero: desfaz
            return false;
        }
        return true;
    }

    public void devolver(Long id, int quantidade) {
        redis.opsForValue().increment(chave(id), quantidade);
    }

    public boolean existe(Long id) {
        return Boolean.TRUE.equals(redis.hasKey(chave(id)));
    }
}