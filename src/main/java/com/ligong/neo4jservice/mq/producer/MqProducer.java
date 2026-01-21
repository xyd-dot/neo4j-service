package com.ligong.neo4jservice.mq.producer;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class MqProducer {

    @Value("${mq.neo4j.topic.data-topic}")
    private String neo4jtopic;

    @Autowired
    private DefaultMQProducer neo4jMessageProducer;

    public void sendMqMessage(List<String> cyphers) {
        try {

            Message message = new Message();
            message.setTopic(neo4jtopic);
            message.setBody(JSON.toJSONString(cyphers).getBytes(StandardCharsets.UTF_8));
            neo4jMessageProducer.setVipChannelEnabled(false);
            SendResult sendRst = neo4jMessageProducer.send(message);

            log.info("MqProducer#sendMqMessage >>> 发送mq消息 message:{},sendResult:{}",JSON.toJSONString(cyphers),JSON.toJSONString(sendRst));

        }catch (Exception e){
            log.error("发送报错mq异常",e);
        }
    }
}
