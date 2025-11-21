package com.iwhalecloud.bss.uba.remote.magic.resource;

import com.iwhalecloud.bss.magic.magicapi.core.config.JsonCodeConstants;
import com.iwhalecloud.bss.magic.magicapi.core.model.JsonCode;
import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceService;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceStorage;
import com.iwhalecloud.bss.magic.magicapi.utils.IoUtils;
import com.iwhalecloud.bss.magic.magicapi.utils.JsonUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RestfulMagicResourceStorage implements MagicResourceStorage<RestSiteInfo>, JsonCodeConstants {

    private MagicResourceService magicResourceService;

    @Override
    public String folder() {
        return "restful";
    }

    @Override
    public String suffix() {
        return ".json";
    }

    @Override
    public Class<RestSiteInfo> magicClass() {
        return RestSiteInfo.class;
    }

    @Override
    public boolean requirePath() {
        return false;
    }

    @Override
    public boolean requiredScript() {
        return false;
    }

    @Override
    public boolean allowRoot() {
        return true;
    }

    @Override
    public String buildMappingKey(RestSiteInfo info) {
        return String.format("%s-%s", info.getKey(), info.getUpdateTime());
    }

    @Override
    public void validate(RestSiteInfo entity) {
        notBlank(entity.getHost(), new JsonCode(2001, "host is required"));
        notBlank(entity.getKey(), new JsonCode(2001, "key is required"));
        isTrue(IoUtils.validateFileName(entity.getKey()), new JsonCode(1026, "restful-Key cannot contain special characters"));
        boolean noneMatchKey = magicResourceService.listFiles("restful:0").stream()
                .map(it -> (RestSiteInfo) it)
                .filter(it -> !it.getId().equals(entity.getId()))
                .noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
        isTrue(noneMatchKey, new JsonCode(1022, "restful-Key is exists"));

        if (entity.getApis() != null && !entity.getApis().isEmpty()) {
            Set<String> codes = new HashSet<>();
            entity.getApis().forEach(api -> {
                isTrue(api.getCode() != null && !api.getCode().isEmpty(), new JsonCode(2001, "api code is required"));
                isTrue(codes.add(api.getCode()), new JsonCode(1022, "api code is duplicated"));
            });
        }
    }

    @Override
    public void setMagicResourceService(MagicResourceService magicResourceService) {
        this.magicResourceService = magicResourceService;
    }

    @Override
    public RestSiteInfo read(byte[] bytes) {
        return JsonUtils.readValue(bytes, RestSiteInfo.class);
    }

    @Override
    public byte[] write(MagicEntity entity) {
        return JsonUtils.toJsonBytes(entity);
    }
}