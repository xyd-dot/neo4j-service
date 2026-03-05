package com.ligong.neo4jservice.mq;

import com.ligong.neo4jservice.mq.consumer.ContractMQConsumerGroupOne;
import com.ligong.neo4jservice.mq.consumer.ContractMQConsumerGroupTwo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BeanConfigMq {
    @Value("${mq.neo4j-mq-server}")
    private String namesrvAddr;
    @Value("${mq.neo4j.topic.data-topic}")
    private String contractTopic;
    @Value("${data-groupone}")
    private String consumerGroupOneName;
    @Value("${data-grouptwo}")
    private String consumerGroupTwoName;

    @Autowired
    ContractMQConsumerGroupTwo contractMQConsumerGroupTwo;

    @Autowired
    ContractMQConsumerGroupOne contractMQConsumerGroupOne;

    @Bean
    public DefaultMQPushConsumer getMQGroupOneConsumer() throws Exception {

        checkParam(consumerGroupOneName,namesrvAddr,contractTopic);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroupOneName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);//从上次消费的offset继续消费。
        consumer.registerMessageListener(contractMQConsumerGroupOne);
        consumer.setInstanceName("ykqInstance");
        try {
            consumer.subscribe(contractTopic,"*");
            consumer.start();
            log.info("已启动Conusmer【gruop:" + consumerGroupOneName + "，监听TOPIC-{" + contractTopic + "}");
        } catch (MQClientException e) {
            log.error("rocketMQ DataSync start fail................");
        }
        return consumer;
    }

    @Bean
    public DefaultMQPushConsumer getMQGroupTwoConsumer() throws Exception {
        checkParam(consumerGroupTwoName,namesrvAddr,contractTopic);
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroupTwoName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);//从上次消费的offset继续消费。
        consumer.registerMessageListener(contractMQConsumerGroupTwo);
        consumer.setInstanceName("ykqInstance");
        try {
            consumer.subscribe(contractTopic,"*");
            consumer.start();
            log.info("已启动Conusmer【gruop:" + consumerGroupTwoName + "，监听TOPIC-{" + contractTopic + "}");
        } catch (MQClientException e) {
            log.error("rocketMQ DataSync start fail................");
        }
        return consumer;
    }


    private void checkParam(String groupName, String nameServer, String topics) throws Exception {
        if (org.springframework.util.StringUtils.isEmpty(groupName)) {
            throw new Exception("groupName is null!");
        }
        if (org.springframework.util.StringUtils.isEmpty(nameServer)) {
            throw new Exception("namesrvAddr is null!");
        }
        if (org.springframework.util.StringUtils.isEmpty(topics)) {
            throw new Exception("topics is null!");
        }
    }



}
