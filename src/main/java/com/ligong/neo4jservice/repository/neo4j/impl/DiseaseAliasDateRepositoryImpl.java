package com.ligong.neo4jservice.repository.neo4j.impl;

import com.ligong.neo4jservice.domain.node.ICDDataNode;
import com.ligong.neo4jservice.repository.neo4j.DiseaseAliasDateRepository;
import com.ligong.neo4jservice.service.neo4j.CypherObject;
import com.ligong.neo4jservice.service.neo4j.DataAbstractService;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 20:37
 */
@Repository
public class DiseaseAliasDateRepositoryImpl extends DataAbstractService implements DiseaseAliasDateRepository {

    @Override
    public List<String> findIcdByDiseaseAlias(String diseaseAlias) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (alias:disease_alias {disease_alias_name: $diseaseAlias}) MATCH (i:icd)-[:HAS_ALIAS]->(alias) RETURN i.icd_name");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseAlias",diseaseAlias);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findIcdsByDiseaseAlias(List<String> diseaseAlias) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (i:icd)-[:HAS_ALIAS]-(alias:disease_alias) WHERE alias.disease_alias_name IN $diseaseAlias RETURN distinct i.icd_name AS icdNames");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseAlias",diseaseAlias);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<ICDDataNode> findIcdNodesByDiseaseAlias(List<String> diseaseAlias) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (i:icd)-[:HAS_ALIAS]-(alias:disease_alias) WHERE alias.disease_alias_name IN $diseaseAlias RETURN i");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseAlias",diseaseAlias);
        cypherObject.setParamMap(param);
        return findList(cypherObject, ICDDataNode.class);
    }

    @Override
    public List<String> findDiseaseByDiseaseAlias(String diseaseAlias) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (alias:disease_alias {disease_alias_name: $diseaseAlias}) MATCH (disease:disease_data)-[:HAS_ALIAS]->(alias) RETURN disease.standard_disease_name");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseAlias",diseaseAlias);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findDiseaseByDiseaseAliasList(List<String> diseaseAliasList) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:disease_data)-[:HAS_ALIAS]->(alias:disease_alias) WHERE alias.disease_alias_name IN $diseaseAlias RETURN distinct d.standard_disease_name AS diseaseNames");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseAlias",diseaseAliasList);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

}