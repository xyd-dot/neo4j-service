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

    List<String> findDiseaseNamesByGeneralName(String generalName);

    List<DiseaseDataNode> findDiseaseNodeByStandardId(Integer standardId);

    List<String> findAliasByDrugStandardId(Integer standardId);

    List<DrugDataNode> findDrugsWithPagination(int standardId,int limit);

    void updateDrugByStandardId(DrugDataNode drugDataNode);

    void save(DrugDataNode drugDataNode);

    void deleteByStandardId(DrugDataNode drugDataNode);


}