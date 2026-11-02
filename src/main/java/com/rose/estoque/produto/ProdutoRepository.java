package com.rose.estoque.produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Produto p set p.quantidade = p.quantidade - :qtd " +
            "where p.id = :id and p.quantidade >= :qtd")
    int baixarEstoque(@Param("id") Long id, @Param("qtd") int qtd);
}