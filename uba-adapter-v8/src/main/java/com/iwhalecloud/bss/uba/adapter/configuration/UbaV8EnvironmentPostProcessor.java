package com.iwhalecloud.bss.uba.adapter.configuration;

import com.ztesoft.zsmart.core.configuation.ConfigurationMgr;
import com.ztesoft.zsmart.core.configuation.tree.ConfigurationNode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class UbaV8EnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("----> UbaV8EnvironmentPostProcessor is running");
        ConfigurationMgr.instance().getDataSets().forEach((name, dataSetNode) -> {
            System.out.println("==========" + name);
            Map<String, Object> config = new HashMap<>();
            dataSetNode.getDataSet().getConfigurationNodePool().getRootNodeList().forEach(node -> {
                getChildrenCfg(node, config);
            });
            environment.getPropertySources().addFirst(new MapPropertySource(name, config));
        });
    }

    public void getChildrenCfg(ConfigurationNode node, Map<String, Object> config){
        if(node.getValue()!=null && !String.valueOf(node.getValue()).isEmpty()){
            config.put(node.getName(), node.getValue());
            System.out.printf("key: %s, value: %s%n",node.getName(), node.getValue());
        }
        if(node.getChildrenDirect()!=null){
            node.getChildrenDirect().forEach(child -> {
                getChildrenCfg(child, config);
            });
        }
    }
}
