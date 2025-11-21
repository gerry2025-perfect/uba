package com.iwhalecloud.bss.uba.mq.provide;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.ResultEntity;
import com.iwhalecloud.bss.uba.common.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.common.exception.UbaException;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueInfo;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueMagicDynamicRegistry;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.ztesoft.zsmart.core.mq.model.CoreMQMessage;
import com.ztesoft.zsmart.core.mq.model.CoreProduceResult;
import com.ztesoft.zsmart.core.mq.model.CoreProduceResultStatus;
import com.ztesoft.zsmart.core.mq.producer.CoreProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;

import java.util.Map;

@Component
@MagicModule("mq")
public class MqModule implements DynamicAttribute<MqModule, MqModule>, DynamicModule<MqModule> {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(MqModule.class);

    private final MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry;

    private CoreProducer coreProducer;
    private MessageQueueInfo messageQueueInfo;

    public MqModule(CoreProducer coreProducer, MessageQueueInfo messageQueueInfo, MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry) {
        this.coreProducer = coreProducer;
        this.messageQueueInfo = messageQueueInfo;
        this.messageQueueMagicDynamicRegistry = messageQueueMagicDynamicRegistry;
    }

    @Autowired
    public MqModule(MessageQueueMagicDynamicRegistry messageQueueMagicDynamicRegistry) {
        this.messageQueueMagicDynamicRegistry = messageQueueMagicDynamicRegistry;
    }

    @Comment("send message,return message ID")
    public ResultEntity send(String topic, String tags, String keys, Map<String,Object> messageBody) {
        logger.debug(String.format(">> send MQ , topic : %s, tags : %s, keys : %s", topic, tags, keys));
        CoreMQMessage message = new CoreMQMessage();
        message.setTopic(topic);
        if(tags!=null && !tags.isEmpty()){
            message.setTags(tags);
        }
        if(keys!=null && !keys.isEmpty()) {
            message.setKeys(keys);
        }
        String jsonMessage = CommonUtils.convertToString(messageBody);
        try {
            message.setBody(jsonMessage.getBytes(messageQueueInfo.getCharacterSet()));
            CoreProduceResult result = coreProducer.send(message);
            logger.debug(String.format("<< send MQ result , messageId : %s, status : %s", result.getMessageId(), result.getStatus()));
            if (CoreProduceResultStatus.SEND_OK.equals(result.getStatus())) {
                return ResultEntity.successResult(result.getMessageId());
            }
            return ResultEntity.failResult(ExceptionDefine.MQ_ERROR_TYPE_SEND_FAIL.getErrorCode(),
                    ExceptionDefine.MQ_ERROR_TYPE_SEND_FAIL.getErrorMsg() + ", result status: %s" , result.getStatus().name());
        }catch (Exception e) {
            logger.error(String.format("<< send MQ fail, topic : %s, tags : %s, keys : %s , body: %s", topic, tags, keys, jsonMessage), e);
            return ResultEntity.errorResult(
                    new UbaException(ExceptionDefine.MQ_ERROR_TYPE_SEND, String.format("topic:%s , message:%s", topic ,jsonMessage),e) ,
                    false, "");
        }
    }

    @Override
    public MqModule getDynamicModule(MagicScriptContext context) {
        Map<String, CoreProducer> producers = messageQueueMagicDynamicRegistry.getProducers();
        CoreProducer currentProducer = null ;
        MessageQueueInfo curMessageQueueInfo;
        if(producers.isEmpty()){
            throw new RuntimeException("producer is empty, please check mq configuration");
        }else if(producers.size()==1){
            currentProducer = producers.values().stream().findFirst().get();
            curMessageQueueInfo = messageQueueMagicDynamicRegistry.getConfigurations().values().stream().findFirst().get();
        }else{
            String mqKey = context.getString(CommonConst.mqDefaultServerCode);
            if(mqKey!=null && producers.containsKey(mqKey)){
                currentProducer = producers.get(mqKey);
            }else if(producers.containsKey(CommonConst.mqDefaultServerCode)){
                currentProducer = producers.get(CommonConst.mqDefaultServerCode);
            }
            if(currentProducer==null){
                throw new RuntimeException(String.format("cannot find mq producer by key: %s and %s" , mqKey , CommonConst.mqDefaultServerCode));
            }
            currentProducer = producers.containsKey(mqKey) ? producers.get(mqKey) : producers.get(CommonConst.mqDefaultServerCode);
            curMessageQueueInfo = messageQueueMagicDynamicRegistry.getConfigurations().get(mqKey);
        }
        return new MqModule(currentProducer, curMessageQueueInfo, messageQueueMagicDynamicRegistry);
    }

    @Override
    public MqModule getDynamicAttribute(String key) {
        CoreProducer currentProducer = messageQueueMagicDynamicRegistry.getProducers().get(key);
        if(currentProducer == null){
            throw new RuntimeException("cannot find mq producer by key:"+key);
        }
        return new MqModule(currentProducer, messageQueueMagicDynamicRegistry.getConfigurations().get(key), messageQueueMagicDynamicRegistry);
    }
}
