package com.ligong.neo4jservice.domain.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

/**
 * description: 
 * @author shendaowei
 * @date 2025/9/29 18:09
 */
@Data
@Node("disease_data")
public class DiseaseDataNode {

    @Id
    private String standard_disease_name;
    private Boolean is_child_disease;
    private Boolean is_chronic_disease;
    private Boolean is_critical_disease;
    private String disease_alias_name;

    //男，女性疾病     男性用药、女性用药、通用用药
    private String gender_category;

    private String createTime;

    private String updateTime;

    @Relationship(type = "HAS_ALIAS", direction = Relationship.Direction.OUTGOING)
    private List<DiseaseAliasNode> diseaseAliases;


}