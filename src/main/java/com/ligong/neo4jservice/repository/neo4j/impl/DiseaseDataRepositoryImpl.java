package com.ligong.neo4jservice.repository.neo4j.impl;

import com.ligong.neo4jservice.domain.node.DiseaseDataNode;
import com.ligong.neo4jservice.domain.node.DrugDataNode;
import com.ligong.neo4jservice.repository.neo4j.DiseaseDataRepository;
import com.ligong.neo4jservice.service.neo4j.CypherObject;
import com.ligong.neo4jservice.service.neo4j.DataAbstractService;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 19:39
 */
@Repository
public class DiseaseDataRepositoryImpl extends DataAbstractService implements DiseaseDataRepository {

    @Override
    public List<String> findAliasesByDiseaseName(String standardDiseaseName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:disease_data {standard_disease_name: $standardDiseaseName})-[:HAS_ALIAS]->(alias:disease_alias) RETURN alias.disease_alias_name");
        Map<String,Object> param = new HashMap<>();
        param.put("standardDiseaseName",standardDiseaseName);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findAliasesByDiseaseNames(List<String> standardDiseaseNames) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:disease_data)-[:HAS_ALIAS]->(alias:disease_alias) WHERE d.standard_disease_name IN $standardDiseaseNames RETURN distinct alias.disease_alias_name AS alias_names");
        Map<String,Object> param = new HashMap<>();
        param.put("standardDiseaseNames",standardDiseaseNames);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findIcdNamesByDiseaseNames(List<String> diseaseNames) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:disease_data)-[:HAS_ALIAS]->(alias:disease_alias)<-[:HAS_ALIAS]-(icd:icd) WHERE d.standard_disease_name IN $diseaseNames RETURN DISTINCT icd.icd_name AS icdName");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseNames",diseaseNames);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<DrugDataNode> findDrugNodesByDiseaseNames(List<String> diseaseNames) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (dis:disease_data)-[:HAS_INDICATION]-(drug:drug) WHERE dis.standard_disease_name IN $diseaseNames RETURN drug");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseNames",diseaseNames);
        cypherObject.setParamMap(param);
        return findList(cypherObject, DrugDataNode.class);
    }

    @Override
    public List<DrugDataNode> findDrugNodesByDiseaseName(String diseaseName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (dis:disease_data)-[:HAS_INDICATION]-(drug:drug) WHERE dis.standard_disease_name = $diseaseName RETURN DISTINCT drug");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseName",diseaseName);
        cypherObject.setParamMap(param);
        return findList(cypherObject, DrugDataNode.class);
    }

    @Override
    public List<String> findIcdNamesByDiseaseName(String diseaseName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:disease_data {standard_disease_name: $diseaseName})-[:HAS_ALIAS]->(alias:disease_alias)<-[:HAS_ALIAS]-(icd:icd) RETURN DISTINCT icd.icd_name");
        Map<String,Object> param = new HashMap<>();
        param.put("diseaseName",diseaseName);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public void updateByDiseaseName(DiseaseDataNode diseaseDataNode) {
        updateByPrimary(diseaseDataNode);
    }
}