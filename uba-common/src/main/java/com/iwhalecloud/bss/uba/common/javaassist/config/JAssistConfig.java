package com.iwhalecloud.bss.uba.common.javaassist.config;

import com.iwhalecloud.bss.uba.common.javaassist.weave.IWeave;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**用来承载做JavaAssist配置信息，在字节码解析逻辑中都从这里拿配置信息，信息设置在系统启动的时候收集，包括字节码拦截范围和叠加逻辑类申明*/
public class JAssistConfig {

    public static final String S_AGENT_ERR_PARAM_NAME = "_agent_Log_Err";

    /**临时目录，从环境变量中获取*/
    public static final String tmpdir = System.getProperty("java.io.tmpdir");
    /**是否保存被加工前的原始类*/
    @Getter
    @Setter
    public String isSaveWeaveClass = System.getProperty("isSaveWeaveClass")==null?"false":System.getProperty("isSaveWeaveClass");
    /**全局排除方法清单*/
    @Getter
    @Setter
    public String excludeMethodCfg = System.getProperty("excludeMethodCfg");
    /**全局排除path清单*/
    @Getter
    @Setter
    public String excludePaths = System.getProperty("excludePaths");

    /**全局排除path清单*/
    @Getter
    @Setter
    public Integer loggerStackDepth = System.getProperty("loggerStackDepth")==null?2:Integer.parseInt(System.getProperty("loggerStackDepth"));


    /**path对应的weave处理类清单*/
    @Getter
    public Map<Path, List<IWeave>> weaves = new HashMap<>();

    @Getter
    private static final JAssistConfig instance = new JAssistConfig();

    /**向全局配置中增加weave对象，主要处理其中path对应的weave清单*/
    public void addWeave(IWeave weave) {
        Path[] paths = weave.getApplyPath();
        for (Path path : paths) {
            if(weaves.containsKey(path)){
                weaves.get(path).add(weave);
            }else{
                List<IWeave> weaveList = new ArrayList<>();
                weaveList.add(weave);
                weaves.put(path, weaveList);
            }
        }
    }

}
