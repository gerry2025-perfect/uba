package com.iwhalecloud.bss.uba.mq.consume;

import com.iwhalecloud.bss.uba.common.ResultEntity;
import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueInfo;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.ztesoft.mq.client.api.consumer.MQMessageHandler;
import com.ztesoft.mq.client.api.consumer.MessageHandlerContext;
import com.ztesoft.mq.client.api.model.MQMessage;
import com.ztesoft.mq.client.impl.consumer.ProcessStatus;
import com.ztesoft.zsmart.core.mq.consumer.CoreMQMessageHandler;
import com.ztesoft.zsmart.core.mq.consumer.CoreMessageHandlerContext;
import com.ztesoft.zsmart.core.mq.consumer.CoreProcessStatus;
import com.ztesoft.zsmart.core.mq.model.CoreMQMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class MagicConsumeHandler implements MQMessageHandler, CoreMQMessageHandler {

    private static final UbaLogger logger = UbaLogger.getLogger(MagicConsumeHandler.class);

    @Getter
    private final MagicRunner magicRunner;
    @Getter
    private final MessageQueueInfo messageQueueInfo;
    @Getter
    private final MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry;

    public MagicConsumeHandler(MagicRunner magicRunner, MessageQueueInfo messageQueueInfo, MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry) {
        this.magicRunner = magicRunner;
        this.messageQueueInfo = messageQueueInfo;
        this.messageQueueMagicDynamicRegistry = messageQueueMagicDynamicRegistry;
    }


    @Override
    public ProcessStatus process(MQMessage mqMessage, MessageHandlerContext messageHandlerContext) {
        ResultEntity resultEntity = execute(mqMessage.getTopic(), mqMessage.getBody(), mqMessage.getKeys(), mqMessage.getTags(),
                mqMessage.getUserProperty(),mqMessage.getSystemProperty());
        if(resultEntity!=null && ResultEntity.ResultFlag.RETRY.equals(resultEntity.getResultFlag())){
            return ProcessStatus.Retry;
        }else {
            return ProcessStatus.Done;
        }
    }

    abstract static class AbstractExecutor {
        String topic;
        String[] keys;
        String[] tags;
        Map<String,String> userProperty;
        Map<String,String> systemProperty;
        MagicConsumeHandler consumeHandler;

        public AbstractExecutor(MagicConsumeHandler consumeHandler, String topic, String[] keys, String[] tags, Map<String,String> userProperty, Map<String,String> systemProperty) {
            this.consumeHandler = consumeHandler;
            this.topic = topic;
            this.keys = keys;
            this.tags = tags;
            this.userProperty = userProperty;
            this.systemProperty = systemProperty;
        }

        /**根据MQ消息信息初始化执行器*/
        abstract Map<String,Object> buildContext();
        /**构造Magic-api的URL*/
        abstract String buildUrl();
        /**正常处理MQ消息之后，收尾逻辑*/
        abstract ResultEntity afterExecute(ResultEntity result);
        /**执行当前执行器*/
        ResultEntity execute(String body){
            Map<String,Object> context = buildContext();
            context.put("body",body);
            return afterExecute(consumeHandler.getMagicRunner().executeMagicApi(buildUrl(), context));
        }
    }

    /**根据topic获取对应topic订阅类型的处理类*/
    private AbstractExecutor getExecutor(String topic, String[] keys, String[] tags, Map<String,String> userProperty, Map<String,String> systemProperty){
        List<MessageQueueInfo.TopicInfo> topicInfoList = messageQueueInfo.getConsumerTopics();
        return topicInfoList.stream().filter((topicInfo -> topicInfo.getTopicName().equals(topic))).map(topicInfo -> {
            if(MessageQueueInfo.TopicConsumerType.BtcTask.equals(topicInfo.getTopicConsumerType())){
                return new BtcTaskExecutor(this, topic, keys, tags, userProperty, systemProperty);
            }else{
                return new DefaultExecutor(this, topic, keys, tags, userProperty, systemProperty);
            }
        }).findFirst().orElse(null);
    }

    private ResultEntity execute(String topic, byte[] body, String keys, String tags,
                                 Map<String,String> userProperty, Map<String,String> systemProperty){
        logger.debug(String.format("receive MQ topic: %s , tags: %s , keys: %s", topic, tags, keys));
        String bodyString ;
        try {
            bodyString = new String(body, messageQueueInfo.getCharacterSet());
        }catch (Exception e){
            throw new RuntimeException(String.format("current Message charset [%s] is not support, please check MQ configuration.",
                    messageQueueInfo.getCharacterSet()), e);
        }
        return getExecutor(topic,
                keys != null ? keys.split(":") : null,
                tags != null ? tags.split(":") : null,
                userProperty, systemProperty).execute(bodyString);
    }

    @Override
    public CoreProcessStatus process(CoreMQMessage mqMessage, CoreMessageHandlerContext messageHandlerContext) {
        ResultEntity resultEntity = execute(mqMessage.getTopic(), mqMessage.getBody(), mqMessage.getKeys(), mqMessage.getTags(),
                mqMessage.getUserProperties(),mqMessage.getSystemProperties());
        if(resultEntity!=null && ResultEntity.ResultFlag.RETRY.equals(resultEntity.getResultFlag())){
            return CoreProcessStatus.Retry;
        }else {
            return CoreProcessStatus.Done;
        }
    }
}
