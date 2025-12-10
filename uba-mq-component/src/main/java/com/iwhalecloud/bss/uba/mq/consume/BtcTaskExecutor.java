package com.iwhalecloud.bss.uba.mq.consume;

import com.iwhalecloud.bss.uba.comm.CommonUtils;
import com.iwhalecloud.bss.uba.comm.ResultEntity;
import com.iwhalecloud.bss.uba.comm.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.comm.exception.UbaException;
import com.iwhalecloud.bss.uba.comm.prop.CommonConst;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.ztesoft.zsmart.core.mq.common.exception.CoreMQClientApiException;
import com.ztesoft.zsmart.core.mq.model.CoreMQMessage;

import java.util.HashMap;
import java.util.Map;

/**用来处理BTC模块发送的任务处理，出入信息都按照BTC模块的要求来*/
public class BtcTaskExecutor extends MagicConsumeHandler.AbstractExecutor {

    private static final UbaLogger logger = UbaLogger.getLogger(BtcTaskExecutor.class);

    /**任务名称*/
    private String taskName;
    /**定时任务实例ID*/
    private Long instanceId;
    /**定时任务实例序列，一条任务被拆解成多个消息的时候用来区分不同消息的顺序*/
    private Long seq;
    /**消息类型*/
    private String msgType;

    public BtcTaskExecutor(MagicConsumeHandler consumeHandler, String topic, String[] keys, String[] tags, Map<String, String> userProperty, Map<String, String> systemProperty) {
        super(consumeHandler, topic, keys, tags, userProperty, systemProperty);
    }

    @Override
    Map<String, Object> buildContext() {
        Map<String, Object> context = new HashMap<>();
        if(keys == null || keys.length != 4){
            throw new UbaException(ExceptionDefine.BTC_ERROR_TYPE_VALIDATE_FAIL,
                    String.format(": The keys in batch task message of BTC module must be 4 part ,current value is: %s", (Object) keys), null);
        }
        taskName = keys[0];
        instanceId = Long.parseLong(keys[1]);
        seq = Long.parseLong(keys[2]);
        msgType = keys[3];
        context.put("taskName", taskName);
        context.put("instanceId", instanceId);
        context.put("seq", seq);
        context.put("msgType", msgType);
        return context;
    }

    @Override
    String buildUrl() {
        return String.format("/btc/%s" , taskName);
    }

    @Override
    ResultEntity afterExecute(ResultEntity result) {
        //要反馈批量任务处理的MQ服务器，应该是接受调度MQ的服务器是同一个，所以直接获取
        CoreMQMessage message = new CoreMQMessage();
        message.setTopic(CommonConst.BTC_RESPONSE_TOPIC);
        try {
            message.setBody(CommonUtils.convertToString(result.getData()).getBytes(consumeHandler.getMessageQueueInfo().getCharacterSet()));
        }catch (Exception e){
            logger.error(String.format(", transfer result data to message body error , btc instanceId : %d , data: %s, charset: %s",
                    instanceId, result.getData(), consumeHandler.getMessageQueueInfo().getCharacterSet()), e);
        }
        try {
            consumeHandler.getMessageQueueMagicDynamicRegistry().getProducer(consumeHandler.getMessageQueueInfo().getKey()).send(message);
        } catch (CoreMQClientApiException e) {
            logger.error(String.format(", send response to BTC by MQ failed , btc instanceId : %d",  instanceId), e);
        }
        return result;
    }
}
