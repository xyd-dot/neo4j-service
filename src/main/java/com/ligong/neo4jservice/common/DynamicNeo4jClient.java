package com.ligong.neo4jservice.common;

import com.ligong.neo4jservice.config.Neo4jDataSourceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

@Component
public class DynamicNeo4jClient {
    @Autowired
    @Qualifier("ds1Neo4jClient")
    private Neo4jClient ds1Neo4jClient;

    @Autowired
    @Qualifier("ds2Neo4jClient")
    private Neo4jClient ds2Neo4jClient;

    public Neo4jClient getClient() {
        String key = Neo4jDataSourceContextHolder.getDataSourceKey();
        if ("ds1".equals(key)) {
            return ds1Neo4jClient;
        } else if ("ds2".equals(key)) {
            return ds2Neo4jClient;
        } else {
            throw new IllegalArgumentException("未配置的数据源: " + key);
        }
    }
}
