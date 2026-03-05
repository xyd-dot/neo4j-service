package com.ligong.neo4jservice.repository.neo4j;

import com.ligong.neo4jservice.domain.node.DiseaseDataNode;
import com.ligong.neo4jservice.domain.node.DrugDataNode;

import java.util.List;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 19:38
 */
public interface DiseaseDataRepository {

    // 通过疾病标准名称查询所有别名
    List<String> findAliasesByDiseaseName(String standardDiseaseName);

    //根据标准名称查询别名
    List<String> findAliasesByDiseaseNames(List<String> standardDiseaseNames);

    //通过疾病名称查询药物
    List<DrugDataNode> findDrugNodesByDiseaseNames(List<String> diseaseNames);

    //通过疾病名称查询药物
    List<DrugDataNode> findDrugNodesByDiseaseName(String diseaseName);

    void updateByDiseaseName(DiseaseDataNode diseaseDataNode);

}