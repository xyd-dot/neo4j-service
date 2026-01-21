package com.ligong.neo4jservice.utils;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/31 15:43
 */
@Data
public class RelationshipFieldInfo {

    private String fieldName;

    private String relationshipType;

    private Relationship.Direction direction;

    private List<Object> relatedObjects;

    private boolean singleObject;
}