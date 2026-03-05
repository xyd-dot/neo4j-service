package com.ligong.neo4jservice.repository.neo4j;

import java.util.List;

/**
 * description:
 * @author shendaowei
 * @date 2025/10/30 20:37
 */
public interface DiseaseAliasDateRepository {

    List<String> findDiseaseByDiseaseAlias(String diseaseAlias);

    List<String> findDiseaseByDiseaseAliasList(List<String> diseaseAliasList);

}