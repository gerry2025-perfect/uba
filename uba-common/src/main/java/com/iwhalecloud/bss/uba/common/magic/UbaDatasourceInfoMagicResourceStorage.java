package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.magic.magicapi.datasource.model.DataSourceInfo;
import com.iwhalecloud.bss.magic.magicapi.datasource.service.DataSourceInfoMagicResourceStorage;
import com.iwhalecloud.bss.magic.magicapi.utils.JsonUtils;
import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.ztesoft.zsmart.core.log.ZSmartLogger;

import java.nio.charset.StandardCharsets;

public class UbaDatasourceInfoMagicResourceStorage extends DataSourceInfoMagicResourceStorage {

	private static final ZSmartLogger logger = ZSmartLogger.getLogger(UbaDatasourceInfoMagicResourceStorage.class);

	@Override
	public DataSourceInfo read(byte[] bytes) {
		String content = new String(bytes, StandardCharsets.UTF_8);
		DataSourceInfo entity ;
		if(MagicContext.getReplaceFlag()) {
			String[] replaceResult = CommonUtils.replaceStr(CommonConst.PATTERN, content, PropertyHolder::getProp);
			entity = JsonUtils.readValue(replaceResult[0], magicClass());
			if (entity != null && !replaceResult[1].isEmpty()) {
				logger.debug(String.format("[%s] [%s] placeholder replace : %s", entity.getName(), entity.getId(), replaceResult[1]));
			}
		}else{
			entity = JsonUtils.readValue(content, magicClass());
		}
		return entity;
	}


}
