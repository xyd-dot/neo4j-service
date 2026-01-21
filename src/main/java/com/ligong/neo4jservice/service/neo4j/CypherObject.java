package com.ligong.neo4jservice.service.neo4j;

import lombok.Data;

import java.util.Map;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/28 11:26
 */
@Data
public class CypherObject {
    private String cypher;
    private Map<String,Object> paramMap;

}