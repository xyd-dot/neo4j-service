package com.ligong.neo4jservice.domain.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * description: 
 * @author shendaowei
 * @date 2025/9/29 18:11
 */
@Data
@Node("symptom_data")
public class SymptomDataNode {
    @Id
    private String standard_symptom_name;
    private String symptom_alias_name;
}