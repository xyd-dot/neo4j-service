package com.ligong.neo4jservice.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.ligong.neo4jservice.common.DynamicNeo4jClient;
import com.ligong.neo4jservice.config.Neo4jDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.neo4j.driver.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ContractMQConsumerGroupTwo implements MessageListenerConcurrently {

    @Autowired
    private DynamicNeo4jClient dynamicNeo4jClient;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        MessageExt messageExt = list.get(0);
        byte[] body = messageExt.getBody();
        String s = new String(body, StandardCharsets.UTF_8);
        List<String> cyphers = JSON.parseArray(s, String.class);
        try {
            Neo4jDataSourceContextHolder.setDataSourceKey("ds2");
            Neo4jClient client = dynamicNeo4jClient.getClient();
            QueryRunner queryRunner = client.getQueryRunner();
            for (String cypher : cyphers) {
                queryRunner.run(cypher);
            }
        } catch (Exception e) {
            log.error("neo4j 节点2 消费失败，cypher语句详情:{}", JSON.toJSONString(cyphers), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
