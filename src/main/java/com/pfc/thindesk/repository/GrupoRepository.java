package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Grupo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends MongoRepository<Grupo, String> {
}
