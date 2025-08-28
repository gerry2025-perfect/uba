package com.iwhalecloud.bss.uba.mq.consume;

import com.iwhalecloud.bss.uba.common.ResultEntity;

import java.util.HashMap;
import java.util.Map;

/**MQ消息订阅到之后默认处理类*/
public class DefaultExecutor extends MagicConsumeHandler.AbstractExecutor {


    public DefaultExecutor(MagicConsumeHandler consumeHandler, String topic, String[] keys, String[] tags, Map<String, String> userProperty, Map<String, String> systemProperty) {
        super(consumeHandler, topic, keys, tags, userProperty, systemProperty);
    }

    @Override
    Map<String, Object> buildContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("topic", topic);
        context.put("tags", tags);
        context.put("keys", keys);
        context.put("userProperty", userProperty);
        context.put("systemProperty", systemProperty);
        return context;
    }

    @Override
    String buildUrl() {
        return String.format("/mq/%s/%s", consumeHandler.getMessageQueueInfo().getKey(), topic);
    }

    @Override
    ResultEntity afterExecute(ResultEntity result) {
        return result;
    }
}
