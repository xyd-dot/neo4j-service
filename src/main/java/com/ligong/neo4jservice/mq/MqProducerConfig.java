package com.ligong.neo4jservice.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class MqProducerConfig {

    @Value("${mq.neo4j-mq-server}")
    private String namesrvAddr;
    @Value("${mq.neo4j.topic.data-topic}")
    private String contractTopic;

    @Bean
    public DefaultMQProducer neo4jMessageProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(contractTopic);
        producer.setNamesrvAddr(namesrvAddr);
        producer.setInstanceName("neo4j-producer-instance");

        try {
            producer.start();
            log.info("【RocketMQ】Neo4j 消息生产者启动成功，namesrvAddr: {}, topic: {}", namesrvAddr, contractTopic);
        } catch (MQClientException e) {
            log.error("【RocketMQ】Neo4j 消息生产者启动失败，namesrvAddr: {}, topic: {}", namesrvAddr, contractTopic, e);
            throw new RuntimeException("RocketMQ Producer 启动失败，系统无法继续运行", e); // 推荐：启动失败则抛异常，阻止应用启动
        }

        return producer;
    }






}
