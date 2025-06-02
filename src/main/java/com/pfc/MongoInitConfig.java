package com.pfc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoInitConfig implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        if (!mongoTemplate.collectionExists("jogos")) {
            mongoTemplate.createCollection("jogos");
        }
        if (!mongoTemplate.collectionExists("usuarios")) {
            mongoTemplate.createCollection("usuarios");
        }
        if (!mongoTemplate.collectionExists("perfis")) {
            mongoTemplate.createCollection("perfis");
        }
        if (!mongoTemplate.collectionExists("grupos")) {
            mongoTemplate.createCollection("grupos");
        }
        if (!mongoTemplate.collectionExists("sugestoesDeJogos")) {
            mongoTemplate.createCollection("sugestoesDeJogos");
        }
        if (!mongoTemplate.collectionExists("depoimentos")) {
            mongoTemplate.createCollection("depoimentos");
        }
    }
}