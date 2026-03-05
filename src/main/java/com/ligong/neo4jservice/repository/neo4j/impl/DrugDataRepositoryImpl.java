package com.ligong.neo4jservice.repository.neo4j.impl;

import com.ligong.neo4jservice.domain.node.DiseaseDataNode;
import com.ligong.neo4jservice.domain.node.DrugDataNode;
import com.ligong.neo4jservice.repository.neo4j.DrugDataRepository;
import com.ligong.neo4jservice.service.neo4j.CypherObject;
import com.ligong.neo4jservice.service.neo4j.DataAbstractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/28 20:07
 */
@Slf4j
@Repository
public class DrugDataRepositoryImpl extends DataAbstractService implements DrugDataRepository {


    public String findDrugNameByStandardId(Integer standardId){
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:drug {standard_id: $standardId}) RETURN n.general_name");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        cypherObject.setParamMap(param);
        return findOne(cypherObject, String.class);
    }

    @Override
    public List<String> findDrugNameByStandardIds(List<Integer> standardIds) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:drug) where n.standard_id in $standardIds RETURN n.general_name");
        Map<String,Object> param = new HashMap<>();
        param.put("standardIds",standardIds);
        cypherObject.setParamMap(param);
        return findList(cypherObject, String.class);
    }

    @Override
    public List<DrugDataNode> findDrugNodeByStandardIds(List<Integer> standardIds) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:drug) WHERE d.standard_id in $standardIds RETURN d");
        Map<String,Object> param = new HashMap<>();
        param.put("standardIds",standardIds);
        cypherObject.setParamMap(param);
        return findList(cypherObject, DrugDataNode.class);
    }

    @Override
    public DrugDataNode findDrugNodeByStandardId(Integer standardId) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:drug {standard_id: $standardId}) RETURN n");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        cypherObject.setParamMap(param);
        return findOne(cypherObject, DrugDataNode.class);
    }

    @Override
    public String findDrugDiseaseByStandardId(Integer standardId) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:drug {standard_id: $standardId}) RETURN n.apply_disease_name");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        cypherObject.setParamMap(param);
        return findOne(cypherObject, String.class);
    }

    @Override
    public List<DrugDataNode> findDrugByGeneralName(String generalName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (n:drug {general_name: $generalName}) RETURN n");
        Map<String,Object> param = new HashMap<>();
        param.put("generalName",generalName);
        cypherObject.setParamMap(param);
        return findList(cypherObject,DrugDataNode.class);
    }

    @Override
    public List<String> findDiseaseNamesByGeneralName(String generalName) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:drug {general_name: $generalName})-[:HAS_INDICATION]->(dis:disease_data) RETURN dis.standard_disease_name");
        Map<String,Object> param = new HashMap<>();
        param.put("generalName",generalName);
        cypherObject.setParamMap(param);
        return findList(cypherObject,String.class);
    }

    @Override
    public List<DiseaseDataNode> findDiseaseNodeByStandardId(Integer standardId) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:drug {standard_id: $standardId})-[:HAS_INDICATION]->(dis:disease_data) RETURN dis");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        cypherObject.setParamMap(param);
        return findList(cypherObject,DiseaseDataNode.class);
    }

    @Override
    public List<String> findAliasByDrugStandardId(Integer standardId) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (drug:drug {standard_id: $standardId}) MATCH (drug)-[:HAS_INDICATION]->(disease:disease_data) MATCH (disease)-[:HAS_ALIAS]->(alias:disease_alias) RETURN alias.disease_alias_name as disease_alias_name");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        cypherObject.setParamMap(param);
        return findList(cypherObject,String.class);
    }

    @Override
    public List<DrugDataNode> findDrugsWithPagination(int standardId, int limit) {
        CypherObject cypherObject = new CypherObject();
        cypherObject.setCypher("MATCH (d:drug) WHERE d.standard_id > $standardId RETURN d ORDER BY d.standard_id ASC LIMIT $limit");
        Map<String,Object> param = new HashMap<>();
        param.put("standardId",standardId);
        param.put("limit",limit);
        cypherObject.setParamMap(param);
        return findList(cypherObject,DrugDataNode.class);
    }

    @Override
    public void updateDrugByStandardId(DrugDataNode drugDataNode) {
        updateByPrimary(drugDataNode);
    }


    @Override
    public void save(DrugDataNode drugDataNode){
        saveOne(drugDataNode);
    }

    @Override
    public void deleteByStandardId(DrugDataNode drugDataNode) {
        deleteOne(drugDataNode);
    }

}