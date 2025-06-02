package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.SugestaoDeJogo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SugestaoDeJogoRepository extends MongoRepository<SugestaoDeJogo, String> {
}
