package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Jogo;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface JogoRepository extends MongoRepository<Jogo, String> {
    //Procura Todos
    List<Jogo> findAll();

    // Busca pleo nome e retorna os resultados em uma página
    Page<Jogo> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

}
