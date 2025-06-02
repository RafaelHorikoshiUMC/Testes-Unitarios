package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Perfil;
import com.pfc.thindesk.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends MongoRepository<Perfil, String> {
    // Buscar todos
    List<Perfil> findAll();

    // Busca por perfil associado a um usuário
    Optional<Perfil> findByUsuarioId(String usuarioId);




}

