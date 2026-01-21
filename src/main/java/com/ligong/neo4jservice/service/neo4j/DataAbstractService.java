package com.ligong.neo4jservice.service.neo4j;

import com.ligong.neo4jservice.common.DynamicNeo4jClient;
import com.ligong.neo4jservice.common.enums.QueryResultType;
import com.ligong.neo4jservice.config.Neo4jDataSourceContextHolder;
import com.ligong.neo4jservice.exception.Neo4jRuntimeException;
import com.ligong.neo4jservice.loadbalance.LoadBalancerService;
import com.ligong.neo4jservice.loadbalance.Server;
import com.ligong.neo4jservice.mq.producer.MqProducer;
import com.ligong.neo4jservice.utils.CypherUtils;
import com.ligong.neo4jservice.utils.CypherUtils3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class DataAbstractService {

    @Autowired
    private DynamicNeo4jClient dynamicNeo4jClient;

    @Autowired
    private LoadBalancerService loadBalancerService;

    @Autowired
    private MqProducer mqProducer;

    /**
     * 查询返回一个单个
     * @param cypherObject [cypherObject]
     * @param clazz [clazz]
     * @return {@link T}
     * @date 2025/11/3 09:51
     */
    protected <T> T findOne(CypherObject cypherObject, Class<T> clazz) {
        List<T> result = query(cypherObject, clazz);
        if(CollectionUtils.isEmpty(result)){
            return null;

        }
        if (result.size() > 2) {
            throw new Neo4jRuntimeException("结果映射出错，映射结果大于1个");
        }
        return result.get(0);
    }

    /**
     * 查询返回列表
     * @param cypherObject [cypherObject]
     * @param clazz [clazz]
     * @return {@link List<T>}
     * @date 2025/11/3 09:52
     */
    protected <T> List<T> findList(CypherObject cypherObject, Class<T> clazz) {
        return query(cypherObject, clazz);
    }

    /**
     * 保存
     * @param data [data]
     * @date 2025/11/3 09:53
     */
    protected <T> void saveOne(T data) {
        String saveCypher = "";
        if (isSingleCypher(data)){
            saveCypher = CypherUtils.getSaveCypher(data);
        }else {
            saveCypher = CypherUtils3.getSaveCypher(data);
        }
        log.info("saveCypher info:{}", saveCypher);
        mqProducer.sendMqMessage(Collections.singletonList(saveCypher));
    }

    protected <T> void deleteOne(T data){
        String deleteCypher = CypherUtils.getDeleteCypher(data);
        log.info("deleteCypher info:{}", deleteCypher);
        mqProducer.sendMqMessage(Collections.singletonList(deleteCypher));
    }

    /**
     * 保存列表
     * @param data [data]
     * @date 2025/11/3 09:53
     */
    protected <T> void updateByPrimary(T data) {
        String updateByPrimary = CypherUtils.getUpdateCypher(data);
        log.info("updateByPrimary info:{}", updateByPrimary);
        mqProducer.sendMqMessage(Collections.singletonList(updateByPrimary));
    }

    /**
     * 创建关系
     * @param node1 [node1]
     * @param node2 [node2]
     * @param primaryValue1 [primaryValue1]
     * @param primaryValue2 [primaryValue2]
     * @param relationName [relationName]
     * @date 2025/11/3 09:56
     */
    protected <T1, T2, E1, E2> void createRelation(T1 node1, T2 node2, E1 primaryValue1, E2 primaryValue2, String relationName) {
        String relationCypher = CypherUtils.getCreateRelationCypher(node1, node2, primaryValue1, primaryValue2, relationName);
        log.info("relationCypher info:{}", relationCypher);
        mqProducer.sendMqMessage(Collections.singletonList(relationCypher));
    }

    /**
     * 创建关系并保存节点
     * @param data [data]
     * @date 2025/11/3 09:56
     */
    protected <T> void saveAndCreateRelation(T data) {
        String saveCypherWithRelation = CypherUtils.getSaveCypherWithRelation(data);
        List<String> list = Collections.singletonList(saveCypherWithRelation);
        mqProducer.sendMqMessage(list);
    }


    private <T> List<T> query(CypherObject cypherObject, Class<T> clazz) {
        return queryFromDs(cypherObject.getCypher(), cypherObject.getParamMap(), clazz);
    }


    private <T> List<T> queryFromDs(String cypher, Map<String, Object> params, Class<T> clazz) {
        Server availableServer = loadBalancerService.getAvailableServer();
        try {
            Neo4jDataSourceContextHolder.setDataSourceKey(availableServer.getDs());
            Neo4jClient client = dynamicNeo4jClient.getClient();

            cypher = CypherUtils.replaceParams(cypher, params);

            List<Map<String, Object>> records = (List<Map<String, Object>>) client.query(cypher).fetch().all();
            if (records.isEmpty()) {
                return Collections.emptyList();
            }
            Map<String, Object> firstRecord = records.get(0);
            QueryResultType resultType = CypherUtils.determineResultType(firstRecord);
            return CypherUtils.processRecordsByType(records, resultType, clazz);

        } catch (Exception e) {
            log.error("调用节点 {} 失败", availableServer.getDs(), e);
            throw new Neo4jRuntimeException("调用节点" + availableServer.getDs() + "失败", e);
        } finally {
            Neo4jDataSourceContextHolder.clearDataSourceKey();
        }
    }

    private static <T> boolean isSingleCypher(T t) {
        try {
            List<Field> relationFields = new ArrayList<>();
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(Relationship.class)) {
                    if (declaredField.get(t) != null) {
                        relationFields.add(declaredField);
                    }
                }
            }
            if (CollectionUtils.isEmpty(relationFields)) {
                return true;
            }
        } catch (Exception e) {
            log.error("isSingleCypher error:",e);
        }
        return false;
    }



}
