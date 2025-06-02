package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Depoimento;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepoimentoRepository extends MongoRepository<Depoimento, String> {
}
