# Estoque Black Friday

API de estoque feita para estudar **alta demanda, concorrência e escalabilidade**.
O desafio: um produto tem 100 unidades e centenas de pessoas tentam comprar ao mesmo
tempo. **O sistema nunca pode vender mais do que tem.**

## Tecnologias

Java 21 · Spring Boot 4 · Spring Data JPA · PostgreSQL · Redis · Nginx · Docker Compose · k6

## Arquitetura

```
                          ┌──► api1 ──┐
Cliente ──► Nginx ────────┤           ├──► PostgreSQL (dados)
         (load balancer)  └──► api2 ──┘    Redis (controle de estoque)
```

## O que eu medi (teste de carga com k6)

Cenário: **500 compras simultâneas** (100 usuários ao mesmo tempo) num produto com **100 unidades**.

| Versão | Vendas aprovadas | Estoque final | p95 | Observação |
|---|---|---|---|---|
| 1. Ler → calcular → gravar | **500** ❌ | 45 | 321 ms | Bug de *lost update*: vendeu 400 unidades que não existiam |
| 2. UPDATE atômico no banco | **100** ✅ | 0 | 495 ms | Correto, mas as compras fazem fila na mesma linha (*hot row*) |
| 3. Redis como porteiro | **100** ✅ | 0 | ~500 ms | Vazão +22% (452 → 551 req/s) e mediana −35%: as recusas não chegam ao banco |
| 4. 2 instâncias + Nginx | **100** ✅ | 0 | — | Estoque correto com escala horizontal |

### O que eu aprendi

- **Lost update:** ler, calcular e gravar em passos separados quebra sob concorrência. A correção foi
  `UPDATE produto SET quantidade = quantidade - 1 WHERE id = ? AND quantidade >= 1`, uma operação atômica.
- **Redis como porteiro:** o `DECR` atômico decide quem conseguiu uma unidade. O banco só recebe as 100
  compras aprovadas, não as 500 tentativas.
- **Por que o p95 não caiu com o Redis:** as compras aprovadas ainda disputam a mesma linha no banco.
  O próximo passo seria confirmar o pedido de forma assíncrona, com uma fila.
- **Nginx e round robin:** sem `zone` (memória compartilhada), cada *worker* do Nginx faz o seu próprio
  revezamento, e as requisições não alternavam entre as instâncias.
- **Warm-up:** com a api2 "fria", a api1 fez 99 de 100 vendas. Com as duas aquecidas, a divisão foi 58/42.
  É por isso que existem *readiness probes* e aquecimento antes de liberar o tráfego.

## Como rodar

```bash
./mvnw clean package -DskipTests
docker compose up -d --build
```

A API fica em `http://localhost:8090` (via Nginx).

```bash
# criar produto
curl -X POST http://localhost:8090/produtos -H "Content-Type: application/json" -d '{"nome":"TV","quantidade":100}'

# comprar
curl -X POST http://localhost:8090/produtos/1/comprar -H "Content-Type: application/json" -d '{"quantidade":1}'

# teste de carga
k6 run -e BASE_URL=http://localhost:8090 -e PRODUTO=1 k6/compra.js
```

## Próximos passos

- Confirmar pedidos de forma assíncrona com Kafka
- Métricas com Spring Actuator
- Teste de concorrência automatizado em JUnit