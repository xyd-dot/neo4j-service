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
 * @date 2025/10/9 17:40
 */
@Node("icd")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ICDDataNode implements Serializable {

    @Id
    private Long id;

    private String icd_code;

    private String icd_name;

    private Integer diagnosis_type;

    private String alias_name;

    private String show_name;

    @Relationship(type = "HAS_ALIAS", direction = Relationship.Direction.OUTGOING)
    private List<DiseaseAliasNode> diseaseAliasNodeList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ICDDataNode that = (ICDDataNode) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}