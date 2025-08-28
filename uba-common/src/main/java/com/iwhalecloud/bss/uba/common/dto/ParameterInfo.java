package com.iwhalecloud.bss.uba.common.dto;

import lombok.Data;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**用来承载报文参数，以树状结构进行全量承载*/
@Data
public class ParameterInfo {

    private String parameterId;//参数ID
    private String parId;//父级参数ID
    private String parameterCode;//参数编码
    private String parameterDesc;//参数描述
    private String dataType;//数据类型
    private String defaultValue;//默认值
    private boolean isMandatory;//是否非空
    //子参数清单
    private Set<ParameterInfo> children = new TreeSet<>(Comparator.comparing(ParameterInfo::getParameterCode));
    /**向对象中增加子参数*/
    public void addChild(ParameterInfo parameterInfo){
        children.add(parameterInfo);
    }

}
