package com.iwhalecloud.bss.uba.service.web;

import com.iwhalecloud.bss.uba.common.dubbo.DubboMetadataFetcher;
import com.iwhalecloud.bss.uba.rest.magic.resource.DubboInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;

import java.util.List;
import java.util.Map;

public class MagicExtController  extends MagicController implements MagicExceptionHandler {

    public MagicExtController(MagicConfiguration configuration) {
        super(configuration);
    }

    @GetMapping("/resource/file/{id}/detail")
    @ResponseBody
    public JsonBean<MagicEntity> detail(@PathVariable("id") String id, MagicHttpServletRequest request) {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        //isTrue(allowVisit(request, Authorization.VIEW, entity), PERMISSION_INVALID);
        return new JsonBean<>(entity);
    }

    /**刷新dubbo下的api信息*/
    @GetMapping("/resource/dubbo/{id}/refresh")
    @ResponseBody
    public JsonBean<MagicEntity> refreshDubbo(@PathVariable("id") String id, MagicHttpServletRequest request) {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if(entity instanceof DubboInfo dubboInfo){
            //对于配置zookeeper://172.16.83.207:26001?backup=172.16.83.208:26001,10.10.179.137:26001完整地址的，只取其中主地址zk进行访问
            String zkAddr = dubboInfo.getRegisterAddr();
            if(zkAddr.contains("?")){
                zkAddr =  zkAddr.substring(0,zkAddr.indexOf("?"));
            }
            if(zkAddr.contains("//")){
                zkAddr = zkAddr.substring(zkAddr.indexOf("//") + 2);
            }
            try(DubboMetadataFetcher dmf = new DubboMetadataFetcher(zkAddr)){
                dubboInfo.getApis().clear();
                Map<String, List<DubboMetadataFetcher.MethodInfo>> serviceMap = dmf.getServiceMethods(dubboInfo.getGroupName());
                if(serviceMap != null){
                    serviceMap.forEach((serviceName, methodInfoList) -> {
                        if(methodInfoList != null && !methodInfoList.isEmpty()){
                            DubboInfo.ApiInfo apiInfo = new DubboInfo.ApiInfo();
                            apiInfo.setServiceName(serviceName);
                            methodInfoList.forEach(apiInfo::addMethod);
                            dubboInfo.addApi(apiInfo);
                        }
                    });
                }
                MagicConfiguration.getMagicResourceService().saveFile(dubboInfo);
                return new JsonBean<>(entity);
            } catch (Exception e) {
                logger.error("refresh dubbo api fail", e);
                return new JsonBean<>(-1, e.getMessage());
            }
        }else{
            return new JsonBean<>(-1, String.format("can not be refresh: current file [%s] is not dubbo", id));
        }
    }

    /**新增MQ*/

}
