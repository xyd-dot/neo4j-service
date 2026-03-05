package com.ligong.neo4jservice.domain.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * description:
 * @author shendaowei
 * @date 2025/9/29 15:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Node("drug")
public class DrugDataNode implements Serializable {

    private String id;

    @Id
    private Integer standard_id;

    private String general_name;

    private String drug_alias_name;

    private String apply_disease_name;

    private String apply_symptom_name;

    private String createTime;

    private String updateTime;

    @Relationship(type = "HAS_INDICATION", direction = Relationship.Direction.OUTGOING)
    private List<DiseaseDataNode> apply_diseases;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrugDataNode that = (DrugDataNode) o;
        return Objects.equals(standard_id, that.standard_id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(standard_id);
    }



}