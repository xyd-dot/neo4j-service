package com.ligong.neo4jservice.repository.neo4j;


import com.ligong.neo4jservice.domain.node.DiseaseDataNode;
import com.ligong.neo4jservice.domain.node.DrugDataNode;

import java.util.List;

/**
 * description:
 * @author shendaowei
 * @date 2025/10/30 19:27
 */
public interface DrugDataRepository {

    String findDrugNameByStandardId(Integer standardId);

    List<String> findDrugNameByStandardIds(List<Integer> standardIds);

    List<DrugDataNode> findDrugNodeByStandardIds(List<Integer> standardIds);

    DrugDataNode findDrugNodeByStandardId(Integer standardId);

    String findDrugDiseaseByStandardId(Integer standardId);

    List<DrugDataNode> findDrugByGeneralName(String generalName);

    //根据药品通用名称查找关联的疾病名称
    List<String> findDiseaseNamesByGeneralName(String generalName);

    //根据药品id查找关联的疾病
    List<DiseaseDataNode> findDiseaseNodeByStandardId(Integer standardId);

    //根据标准库id查找药品疾病别名
    List<String> findAliasByDrugStandardId(Integer standardId);

    // 分页查询药品
    List<DrugDataNode> findDrugsWithPagination(int standardId,int limit);

    void updateDrugByStandardId(DrugDataNode drugDataNode);

    void save(DrugDataNode drugDataNode);

    void deleteByStandardId(DrugDataNode drugDataNode);

    //临时方法
    List<DrugDataNode> findByTime();


}