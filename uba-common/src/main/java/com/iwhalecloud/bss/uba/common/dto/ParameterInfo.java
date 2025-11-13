package com.iwhalecloud.bss.uba.common.dto;

import org.ssssssss.magicapi.core.model.BaseDefinition;

import java.util.ArrayList;

/**用来承载报文参数，以树状结构进行全量承载*/
public class ParameterInfo extends BaseDefinition {

    //子参数清单
    private ArrayList<ParameterInfo> children = new ArrayList<>();

    /**向对象中增加子参数*/
    public void addChild(ParameterInfo parameterInfo){
        children.add(parameterInfo);
    }

}
