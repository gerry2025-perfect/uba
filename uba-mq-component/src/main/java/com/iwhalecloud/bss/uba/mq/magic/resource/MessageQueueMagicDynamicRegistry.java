package com.iwhalecloud.bss.uba.mq.magic.resource;

import com.iwhalecloud.bss.uba.common.magic.MagicRunner;
import com.iwhalecloud.bss.uba.mq.MQOperateBuilder;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.ztesoft.zsmart.core.mq.CoreMQClientFactory;
import com.ztesoft.zsmart.core.mq.common.exception.CoreMQClientApiException;
import com.ztesoft.zsmart.core.mq.consumer.CoreConsumer;
import com.ztesoft.zsmart.core.mq.producer.CoreProducer;
import lombok.Getter;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;

import java.util.*;

public class MessageQueueMagicDynamicRegistry extends AbstractMagicDynamicRegistry<MessageQueueInfo> {

	private static final ZSmartLogger logger = ZSmartLogger.getLogger(MessageQueueMagicDynamicRegistry.class);

	private final CoreMQClientFactory mqClientFactory;
	private final MagicRunner magicRunner;

	private final Map<String,CoreConsumer> coreConsumers = new HashMap<>();
	private final Map<String, CoreProducer> coreProducers = new HashMap<>();
	//配置信息
	@Getter
	private final Map<String,MessageQueueInfo> configurations = new HashMap<>();

	/**根据MQ server key获取对应生产者，用来发送MQ*/
	public CoreProducer getProducer(String mqKey){
		return coreProducers.get(mqKey);
	}

	/**获取全量MQ server生产者实例*/
	public Map<String,CoreProducer> getProducers() {
		return coreProducers;
	}

	/**在应用退出的时候关闭链接信息*/
	@EventListener
	public void destroyResource(ContextClosedEvent contextClosedEvent) {
		if(!coreConsumers.isEmpty()){
			coreConsumers.values().forEach(consumer -> {
				try {
					consumer.close();
				}catch (Exception e){
					logger.warn("release consumer failed:" + e.getMessage());
				}
			});
		}
		if(!coreConsumers.isEmpty()){
			coreProducers.values().forEach(producer -> {
				try {
					producer.close();
				}catch (Exception e){
					logger.warn("release producer failed:" + e.getMessage());
				}
			});
		}
	}

	public MessageQueueMagicDynamicRegistry(MagicResourceStorage<MessageQueueInfo> magicResourceStorage, CoreMQClientFactory mqClientFactory, MagicRunner magicRunner) {
		super(magicResourceStorage);
		this.mqClientFactory = mqClientFactory;
		this.magicRunner = magicRunner;
	}

	@EventListener(condition = "#event.type == 'mq'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("MessageQueueInfo register/unregister messageQueue fail", e);
		}
	}

	@Override
	protected boolean register(MappingNode<MessageQueueInfo> mappingNode) {
		MessageQueueInfo info = mappingNode.getEntity();

		Arrays.stream(info.getDirect().split(",")).forEach(direct -> {
			if(MessageQueueInfo.DIRECT_CONSUMER.equals(direct)){
				try {
					logger.debug(String.format(">> init MQ consumer , mq server key:%s -- name:%s", info.getKey(), info.getName()));
					CoreConsumer consumer = new MQOperateBuilder().initConsumer(info,mqClientFactory,magicRunner,this);
					consumer.start();
					logger.debug(String.format("<< MQ consumer start success , mq server key:%s", info.getKey()));
					coreConsumers.put(info.getKey(),consumer);
				} catch (CoreMQClientApiException e) {
					logger.warn(String.format("MQ Consumer for provider [%s] start fail", info.getKey()), e);
				}
			}else if(MessageQueueInfo.DIRECT_PRODUCER.equals(direct)){
				try {
					logger.debug(String.format(">> init MQ producer , mq server key:%s -- name:%s", info.getKey(), info.getName()));
					CoreProducer producer = new MQOperateBuilder().initProducer(info,mqClientFactory);
					producer.start();
					logger.debug(String.format("<< MQ producer initial success , mq server key:%s", info.getKey()));
					coreProducers.put(info.getKey(),producer);
				} catch (CoreMQClientApiException e) {
					logger.warn(String.format("MQ producer for server [%s] start fail", info.getKey()), e);
				}
			}
		});
		configurations.put(info.getKey(),info);
        return true;
	}

	@Override
	protected void unregister(MappingNode<MessageQueueInfo> mappingNode) {
		MessageQueueInfo info = mappingNode.getEntity();
		if(coreConsumers.containsKey(info.getKey())){
            try {
				logger.debug(String.format(">> MQ consumer prepare to shutdown , mq server key:%s -- name:%s", info.getKey(), info.getName()));
                coreConsumers.get(info.getKey()).shutdown();
				logger.debug(String.format("<< MQ consumer shutdown success , mq server key:%s", info.getKey()));
            } catch (CoreMQClientApiException e) {
				logger.warn(String.format("MQ Consumer for provider [%s] shutdown fail", info.getKey()), e);
            }
        }
		if(coreProducers.containsKey(info.getKey())){
            try {
				logger.debug(String.format(">> MQ producer prepare to shutdown , mq server key:%s -- name:%s", info.getKey(), info.getName()));
                coreProducers.get(info.getKey()).shutdown();
				logger.debug(String.format("<< MQ producer shutdown success , mq server key:%s", info.getKey()));
            } catch (CoreMQClientApiException e) {
				logger.warn(String.format("MQ producer for server [%s] shutdown fail", info.getKey()), e);
            }
        }
		configurations.remove(info.getKey());
	}

}
