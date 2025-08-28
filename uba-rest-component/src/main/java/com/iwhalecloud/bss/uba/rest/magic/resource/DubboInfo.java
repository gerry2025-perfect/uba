package com.iwhalecloud.bss.uba.rest.magic.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ssssssss.magicapi.core.model.MagicEntity;

@EqualsAndHashCode(callSuper = true)
@Data
public class DubboInfo extends MagicEntity {

    /**Dubbo服务端的唯一键*/
    private String key;
    /**Dubbo服务端的地址*/
    private String registerAddr;
    /**超时时间*/
    private Integer timeout;
    /**Dubbo服务端组名称*/
    private String groupName;

    @Override
    public MagicEntity simple() {
        DubboInfo dubboInfo = new DubboInfo();
        super.simple(dubboInfo);
        dubboInfo.setKey(this.key);
        return dubboInfo;
    }

    @Override
    public MagicEntity copy() {
        DubboInfo dubboInfo = new DubboInfo();
        super.copyTo(dubboInfo);
        dubboInfo.setGroupName(this.groupName);
        dubboInfo.setKey(this.key);
        dubboInfo.setName(this.name);
        dubboInfo.setTimeout(this.timeout);
        dubboInfo.setRegisterAddr(this.registerAddr);
        return dubboInfo;
    }
    
}
