package com.ligong.neo4jservice.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;

@Configuration
public class Neo4jDs1Config {
    @Value("${neo4j.ds1.uri}")
    private String uri;
    @Value("${neo4j.ds1.username}")
    private String username;
    @Value("${neo4j.ds1.password}")
    private String password;

    @Bean("ds1Driver")
    public Driver ds1Driver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    @Bean("ds1Neo4jClient")
    public Neo4jClient ds1Neo4jClient(@Qualifier("ds1Driver") Driver driver) {
        return Neo4jClient.create(driver);
    }

    @Bean("ds1Neo4jTemplate")
    public Neo4jTemplate ds1Neo4jTemplate(@Qualifier("ds1Neo4jClient") Neo4jClient neo4jClient) {
        return new Neo4jTemplate(neo4jClient);
    }
}
