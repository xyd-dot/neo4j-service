package com.ligong.neo4jservice.repository.neo4j.impl;

import com.ligong.neo4jservice.domain.node.ICDDataNode;
import com.ligong.neo4jservice.repository.neo4j.IcdDataRepository;
import com.ligong.neo4jservice.service.neo4j.CypherObject;
import com.ligong.neo4jservice.service.neo4j.DataAbstractService;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 20:16
 */
@Repository
public class IcdDataRepositoryImpl extends DataAbstractService implements IcdDataRepository {

    @Override
    public List<String> findDiseaseNamesByIcdNames(List<String> icdNames) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (icd:icd)-[:HAS_ALIAS]->(alias:disease_alias)<-[:HAS_ALIAS]-(d:disease_data) WHERE icd.icd_name IN $icdNames RETURN DISTINCT d.standard_disease_name AS diseaseName");
        Map<String,Object> param = new HashMap<>();
        param.put("icdNames",icdNames);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findAliasByIcdNames(List<String> icdNames) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (icd:icd)-[:HAS_ALIAS]->(alias:disease_alias) WHERE icd.icd_name IN $icdNames RETURN distinct alias.disease_alias_name  AS diseaseNames");
        Map<String,Object> param = new HashMap<>();
        param.put("icdNames",icdNames);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findAliasByIcdName(String icdName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (icd:icd)-[:HAS_ALIAS]->(alias:disease_alias) WHERE icd.icd_name = $icdName RETURN distinct alias.disease_alias_name  AS diseaseNames");
        Map<String,Object> param = new HashMap<>();
        param.put("icdName",icdName);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<String> findDiseaseNamesByIcdName(String icdName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (icd:icd)-[:HAS_ALIAS]->(alias:disease_alias)<-[:HAS_ALIAS]-(d:disease_data) WHERE icd.icd_name = $icdName RETURN DISTINCT d.standard_disease_name AS diseaseName");
        Map<String,Object> param = new HashMap<>();
        param.put("icdName",icdName);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public void updateIcdShowName(ICDDataNode icdDataNode) {
        updateByPrimary(icdDataNode);
    }

    @Override
    public ICDDataNode findIcdById(Long id) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:icd{id:$id}) RETURN n ");
        Map<String,Object> param = new HashMap<>();
        param.put("id",id);
        cypherObject.setParamMap(param);
        return findOne(cypherObject, ICDDataNode.class);
    }

    @Override
    public List<ICDDataNode> findIcdWithPagination(int startId, int limit) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (i:icd) WHERE i.id > $startId RETURN i ORDER BY i.id ASC LIMIT $limit");
        Map<String,Object> param = new HashMap<>();
        param.put("startId",startId);
        param.put("limit",limit);
        cypherObject.setParamMap(param);
        return findList(cypherObject, ICDDataNode.class);
    }


}