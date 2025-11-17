package com.iwhalecloud.bss.uba.remote.magic.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ssssssss.magicapi.core.model.MagicEntity;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestSiteInfo extends MagicEntity {

    private String key;
    private String host;
    private Integer port;
    private String contextPath;
    private Map<String, String> defaultHeaders = new HashMap<>();
    private Set<RestEndpoint> apis = new TreeSet<>(Comparator.comparing(RestEndpoint::getCode));

    public void addApi(RestEndpoint endpoint) {
        apis.add(endpoint);
    }

    @Override
    public MagicEntity simple() {
        RestSiteInfo siteInfo = new RestSiteInfo();
        super.simple(siteInfo);
        siteInfo.setKey(this.key);
        return siteInfo;
    }

    @Override
    public MagicEntity copy() {
        RestSiteInfo siteInfo = new RestSiteInfo();
        super.copyTo(siteInfo);
        siteInfo.setName(this.name);
        siteInfo.setKey(this.key);
        siteInfo.setHost(this.host);
        siteInfo.setPort(this.port);
        siteInfo.setContextPath(this.contextPath);
        siteInfo.setDefaultHeaders(this.defaultHeaders);
        return siteInfo;
    }
}