package com.iwhalecloud.bss.uba.remote.magic.resource;

import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.iwhalecloud.bss.uba.remote.module.RestSiteOperator;
import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;
import com.iwhalecloud.bss.magic.magicapi.core.event.FileEvent;
import com.iwhalecloud.bss.magic.magicapi.core.service.AbstractMagicDynamicRegistry;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceStorage;

import java.util.HashMap;
import java.util.Map;

public class RestfulMagicDynamicRegistry extends AbstractMagicDynamicRegistry<RestSiteInfo> {

    private static final UbaLogger logger = UbaLogger.getLogger(RestfulMagicDynamicRegistry.class);

    private final RestTemplate restTemplate;
    @Getter
    private final Map<String, RestSiteOperator> operators = new HashMap<>();

    public RestfulMagicDynamicRegistry(MagicResourceStorage<RestSiteInfo> magicResourceStorage, RestTemplate restTemplate) {
        super(magicResourceStorage);
        this.restTemplate = restTemplate;
    }

    @EventListener(condition = "#event.type == 'restful'")
    public void onFileEvent(FileEvent event) {
        try {
            processEvent(event);
        } catch (Exception e) {
            logger.error("restful register/unregister fail", e);
        }
    }

    @Override
    protected boolean register(MappingNode<RestSiteInfo> mappingNode) {
        RestSiteInfo info = mappingNode.getEntity();
        RestSiteOperator operator = new RestSiteOperator(info, restTemplate);
        operators.put(info.getKey(), operator);
        return true;
    }

    @Override
    protected void unregister(MappingNode<RestSiteInfo> mappingNode) {
        RestSiteInfo info = mappingNode.getEntity();
        operators.remove(info.getKey());
    }
}