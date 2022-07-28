package com.gruppo3.ai.lab3.config;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.gruppo3.ai.lab3")
public class MongoConfig extends AbstractMongoConfiguration {

    @Override
    public MongoClient mongoClient() {
        return new MongoClient("localhost", 27017);
        // in locale, senza Docker, host: localhost
        // con Docker, host: gruppo3mongo
    }

    @Override
    protected String getDatabaseName() {
        return "dblab3";
    }
}
