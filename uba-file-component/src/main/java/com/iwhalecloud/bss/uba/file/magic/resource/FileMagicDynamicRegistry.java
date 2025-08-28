package com.iwhalecloud.bss.uba.file.magic.resource;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;

import java.util.HashMap;
import java.util.Map;

@Component
public class FileMagicDynamicRegistry extends AbstractMagicDynamicRegistry<FileInfo> {

	private static final ZSmartLogger logger = ZSmartLogger.getLogger(FileMagicDynamicRegistry.class);

	private static Map<String,FileInfo> fileInfoMap = new HashMap<>();

	public FileMagicDynamicRegistry(MagicResourceStorage<FileInfo> magicResourceStorage) {
		super(magicResourceStorage);
	}

	public FileInfo getFileInfo(String key){
		return fileInfoMap.get(key);
	}

	/**在应用退出的时候删除内存中的文件配置信息*/
	@EventListener
	public void destroyResource(ContextClosedEvent contextClosedEvent) {
		if(!fileInfoMap.isEmpty()){
			fileInfoMap.clear();
		}
	}

	@EventListener(condition = "#event.type == 'file'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("fileInfo register/unregister messageQueue fail", e);
		}
	}

	@Override
	protected boolean register(MappingNode<FileInfo> mappingNode) {
		FileInfo info = mappingNode.getEntity();
		logger.info("register file info, key: " + info.getKey());
		if(logger.isDebugEnabled()){
			logger.debug(String.format("file info : %s", CommonUtils.convertToString(info)));
		}
		fileInfoMap.put(info.getKey(), info);
        return true;
	}

	@Override
	protected void unregister(MappingNode<FileInfo> mappingNode) {
		FileInfo info = mappingNode.getEntity();
		logger.info("unregister file info, key: " + info.getKey());
		fileInfoMap.remove(info.getKey());
	}

	public Map<String, FileInfo> getFileInfos() {
		return fileInfoMap;
	}
}
