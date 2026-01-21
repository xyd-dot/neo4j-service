package com.ligong.neo4jservice.repository.neo4j;


import com.ligong.neo4jservice.domain.node.ICDDataNode;

import java.util.List;

/**
 * description: 
 * @author shendaowei
 * @date 2025/10/30 20:16
 */
public interface IcdDataRepository {

    //通过icd名称查询疾病
    List<String> findDiseaseNamesByIcdNames(List<String> icdNames);

    //根据icd查找别名
    List<String> findAliasByIcdNames(List<String> icdNames);

    List<String> findAliasByIcdName(String icdName);

    //通过icd名称查询疾病
    List<String> findDiseaseNamesByIcdName(String icdName);

    void updateIcdShowName(ICDDataNode icdDataNode);

    //根据id查找icd
    ICDDataNode findIcdById(Long id);

    // 分页查询icd
    List<ICDDataNode> findIcdWithPagination(int startId, int limit);
}