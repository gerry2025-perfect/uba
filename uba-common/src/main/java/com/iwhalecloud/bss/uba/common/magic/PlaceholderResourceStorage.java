package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.magic.magicapi.core.config.JsonCodeConstants;
import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceStorage;
import com.iwhalecloud.bss.magic.magicapi.utils.JsonUtils;
import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.prop.CommonConst;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;

import java.nio.charset.StandardCharsets;

public abstract class PlaceholderResourceStorage<T extends MagicEntity> implements MagicResourceStorage<T>, JsonCodeConstants {

    private static final UbaLogger logger = UbaLogger.getLogger(PlaceholderResourceStorage.class);

    @Override
    public T read(byte[] bytes) {
        String content = new String(bytes, StandardCharsets.UTF_8);
        T entity ;
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
