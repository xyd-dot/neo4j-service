package com.ligong.neo4jservice.domain.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/9 15:00
 */
@Data
@Node("disease_alias")
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseAliasNode {

    private String id;

    @Id
    private String disease_alias_name;
}