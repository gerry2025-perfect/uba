package com.iwhalecloud.bss.uba.mq.magic.resource;

import com.iwhalecloud.bss.uba.common.dto.ParameterInfo;
import com.iwhalecloud.bss.uba.comm.prop.CommonConst;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MessageQueueInfo extends MagicEntity {
    /**订阅者*/
    public final static String DIRECT_CONSUMER = "consumer";
    /**生产者*/
    public final static String DIRECT_PRODUCER = "producer";
    /**生产者 && 消费者*/
    public final static String DIRECT_PRODUCER_CONSUMER = "all";

    /**MQ服务器的唯一键*/
    private String key;
    private String accessKey;
    private String secretKey;
    /**MQ服务器的地址*/
    private String namesrvAddr;
    /**MQ消息的类型：zmq,ctgmq,alimq或kafka*/
    private String type;
    /**MQ角色：consumer, producer*/
    private String direct;
    /**MQ消息的字符集*/
    private String characterSet;
    /**MQ订阅topic清单*/
    private List<TopicInfo> consumerTopics = new ArrayList<>();
    /**MQ生产topic清单*/
    private List<TopicInfo> producerTopics = new ArrayList<>();

    /**从MQ配置中获取字符集*/
    public String getCharacterSet(){
        String characterSet = this.characterSet;
        if(characterSet==null || characterSet.isEmpty()){
            return CommonConst.DEFAULT_CHARSET;
        }else{
            return characterSet;
        }
    }

    @Override
    public MagicEntity simple() {
        MessageQueueInfo messageQueueInfo = new MessageQueueInfo();
        super.simple(messageQueueInfo);
        messageQueueInfo.setKey(this.key);
        return messageQueueInfo;
    }

    @Override
    public MagicEntity copy() {
        MessageQueueInfo messageQueueInfo = new MessageQueueInfo();
        super.copyTo(messageQueueInfo);
        messageQueueInfo.setType(this.type);
        messageQueueInfo.setKey(this.key);
        messageQueueInfo.setName(this.name);
        messageQueueInfo.setDirect(this.direct);
        messageQueueInfo.setAccessKey(this.accessKey);
        messageQueueInfo.setSecretKey(this.secretKey);
        messageQueueInfo.setCharacterSet(this.characterSet);
        return messageQueueInfo;
    }

    @Data
    public static class TopicInfo{
        //topic名称
        private String topicName;
        //topic参数定义
        private List<ParameterInfo> messageDefine = new ArrayList<>();
        //样例报文
        private String example ;
        //topic订阅类型：Normal、BtcTask
        private TopicConsumerType topicConsumerType = TopicConsumerType.Normal;
    }

    public enum TopicConsumerType{
        BtcTask, Normal
    }
    
}
