package com.iwhalecloud.bss.uba.rest.magic.resource;

import com.iwhalecloud.bss.uba.common.dubbo.DubboOperator;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;

import java.util.HashMap;
import java.util.Map;

public class DubboMagicDynamicRegistry extends AbstractMagicDynamicRegistry<DubboInfo> {

	private static final ZSmartLogger logger = ZSmartLogger.getLogger(DubboMagicDynamicRegistry.class);

	private final Map<String, DubboOperator> dubboOperatorMap = new HashMap<>();

	public DubboMagicDynamicRegistry(MagicResourceStorage<DubboInfo> magicResourceStorage) {
		super(magicResourceStorage);
	}

	/**获取可使用的dubbo服务端*/
	public Map<String,DubboOperator> getDubboOperators() {
		return dubboOperatorMap;
	}

	@EventListener(condition = "#event.type == 'dubbo'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("dubbo register/unregister fail", e);
		}
	}

	@Override
	protected boolean register(MappingNode<DubboInfo> mappingNode) {
		DubboInfo info = mappingNode.getEntity();
		logger.debug(">> dubbo provider register , key:", info.getKey());
		DubboOperator.ConfigInfo configInfo = new DubboOperator.ConfigInfo();
		configInfo.setRegisterAddress(info.getRegisterAddr());
		configInfo.setTimeout(info.getTimeout());
		configInfo.setGroupName(info.getGroupName());
		configInfo.setSourceCode(info.getKey());
		dubboOperatorMap.put(info.getKey(),DubboOperator.getInstance(info.getKey(),configInfo));
		logger.debug(String.format("<< dubbo provider register success , key: %s , timeout: %d, group: %s",
				info.getKey(),  info.getTimeout(), info.getGroupName()));
        return true;
	}

	@Override
	protected void unregister(MappingNode<DubboInfo> mappingNode) {
		DubboInfo info = mappingNode.getEntity();
		DubboOperator.remove(info.getKey());
		dubboOperatorMap.remove(info.getKey());
		logger.debug(String.format("<< dubbo provider unregister , key: %s", info.getKey()));
	}

}
