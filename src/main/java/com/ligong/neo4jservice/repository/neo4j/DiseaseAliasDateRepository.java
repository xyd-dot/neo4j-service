package com.ligong.neo4jservice.repository.neo4j;

import com.ligong.neo4jservice.domain.node.ICDDataNode;

import java.util.List;

/**
 * description:
 * @author shendaowei
 * @date 2025/10/30 20:37
 */
public interface DiseaseAliasDateRepository {

    //根据疾病别名查找关联的icd
    List<String> findIcdByDiseaseAlias(String diseaseAlias);

    //根据疾病别名查找关联的icd
    List<String> findIcdsByDiseaseAlias(List<String> diseaseAlias);

    List<ICDDataNode> findIcdNodesByDiseaseAlias(List<String> diseaseAlias);

    //根据疾病别名查找关联的疾病
    List<String> findDiseaseByDiseaseAlias(String diseaseAlias);

    //根据疾病别名查找关联的疾病
    List<String> findDiseaseByDiseaseAliasList(List<String> diseaseAliasList);

}