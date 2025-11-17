package com.iwhalecloud.bss.uba.service.web;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.dubbo.DubboMetadataFetcher;
import com.iwhalecloud.bss.uba.file.magic.resource.FileInfo;
import com.iwhalecloud.bss.uba.file.operator.FileOperatorFactory;
import com.iwhalecloud.bss.uba.file.operator.IFileOperator;
import com.iwhalecloud.bss.uba.mq.magic.resource.MessageQueueInfo;
import com.iwhalecloud.bss.uba.remote.magic.resource.DubboInfo;
import com.iwhalecloud.bss.uba.remote.magic.resource.RestEndpoint;
import com.iwhalecloud.bss.uba.remote.magic.resource.RestSiteInfo;
import org.springframework.web.bind.annotation.*;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.servlet.MagicHttpServletRequest;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
            return new JsonBean<>(-1, String.format("can not be refresh: current file [%s] is not dubbo resource", id));
        }
    }

    /**新增MQ topic
     * @param topicType topic类型，consumer 订阅者；producer 生产者；all 生产者 + 消费者
     * */
    @PostMapping("/resource/mq/{id}/topic/{topicType}")
    @ResponseBody
    public JsonBean<MagicEntity> saveMQTopic(@PathVariable("id") String id, @PathVariable("topicType") String topicType, MagicHttpServletRequest request) throws IOException {
        //新增的时候需要校验topic是否已存在
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if(entity instanceof MessageQueueInfo messageQueueInfo){
            byte[] bytes = IoUtils.bytes(request.getInputStream());
            MessageQueueInfo.TopicInfo topicInfo = JsonUtils.readValue(bytes, MessageQueueInfo.TopicInfo.class);
            boolean isConsumer = MessageQueueInfo.DIRECT_CONSUMER.equals(topicType) || MessageQueueInfo.DIRECT_PRODUCER_CONSUMER.equals(topicType);
            if(isExistTopic(messageQueueInfo.getConsumerTopics(), topicInfo) && isConsumer){
                return new JsonBean<>(-1, String.format("save fail: topic [%s] exist in consumer topic list", topicInfo.getTopicName()));
            }
            boolean isProducer = (MessageQueueInfo.DIRECT_PRODUCER.equals(topicType) || MessageQueueInfo.DIRECT_PRODUCER_CONSUMER.equals(topicType));
            if(isExistTopic(messageQueueInfo.getProducerTopics(), topicInfo) && isProducer){
                return new JsonBean<>(-1, String.format("save fail: topic [%s] exist in producer topic list", topicInfo.getTopicName()));
            }
            if(isConsumer){
                replaceTopic(messageQueueInfo.getConsumerTopics(), topicInfo);
            }
            if(isProducer){
                replaceTopic(messageQueueInfo.getProducerTopics(), topicInfo);
            }
            //自动更新MQ定义上的范围
            if(isConsumer && (messageQueueInfo.getDirect()==null || messageQueueInfo.getDirect().isEmpty())){
                messageQueueInfo.setDirect(MessageQueueInfo.DIRECT_CONSUMER);
            }
            if(isProducer && (messageQueueInfo.getDirect()==null || messageQueueInfo.getDirect().isEmpty())){
                messageQueueInfo.setDirect(MessageQueueInfo.DIRECT_PRODUCER);
            }
            if(isConsumer && MessageQueueInfo.DIRECT_PRODUCER.equals(messageQueueInfo.getDirect())){
                messageQueueInfo.setDirect(MessageQueueInfo.DIRECT_PRODUCER_CONSUMER);
            }
            if(isProducer && MessageQueueInfo.DIRECT_PRODUCER_CONSUMER.equals(messageQueueInfo.getDirect())){
                messageQueueInfo.setDirect(MessageQueueInfo.DIRECT_PRODUCER_CONSUMER);
            }
            MagicConfiguration.getMagicResourceService().saveFile(messageQueueInfo);
            return new JsonBean<>(entity);
        }else{
            return new JsonBean<>(-1, String.format("save fail: current file [%s] is not MQ resource", id));
        }
    }

    /**修改MQ topic*/
    @PatchMapping("/resource/mq/{id}/topic")
    @ResponseBody
    public JsonBean<MagicEntity> updateMQTopic(@PathVariable("id") String id, MagicHttpServletRequest request) throws IOException {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if(entity instanceof MessageQueueInfo messageQueueInfo){
            byte[] bytes = IoUtils.bytes(request.getInputStream());
            MessageQueueInfo.TopicInfo topicInfo = JsonUtils.readValue(bytes, MessageQueueInfo.TopicInfo.class);
            if(isExistTopic(messageQueueInfo.getConsumerTopics(), topicInfo)){
                replaceTopic(messageQueueInfo.getConsumerTopics(), topicInfo);
            }
            if(isExistTopic(messageQueueInfo.getProducerTopics(), topicInfo)){
                replaceTopic(messageQueueInfo.getProducerTopics(), topicInfo);
            }
            MagicConfiguration.getMagicResourceService().saveFile(messageQueueInfo);
            return new JsonBean<>(entity);
        }else{
            return new JsonBean<>(-1, String.format("save fail: current file [%s] is not MQ resource", id));
        }
    }

    /**删除MQ topic*/
    @DeleteMapping("/resource/mq/{id}/topic/{topicName}")
    @ResponseBody
    public JsonBean<MagicEntity> deleteMQTopic(@PathVariable("id") String id, @PathVariable("topicName") String topicName) {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if (entity instanceof MessageQueueInfo messageQueueInfo) {
            boolean removed = messageQueueInfo.getConsumerTopics().removeIf(topic -> topic.getTopicName().equals(topicName));
            removed |= messageQueueInfo.getProducerTopics().removeIf(topic -> topic.getTopicName().equals(topicName));
            if (removed) {
                MagicConfiguration.getMagicResourceService().saveFile(messageQueueInfo);
                return new JsonBean<>(entity);
            } else {
                return new JsonBean<>(-1, String.format("delete fail: topic [%s] not found", topicName));
            }
        } else {
            return new JsonBean<>(-1, String.format("delete fail: current file [%s] is not MQ resource", id));
        }
    }

    /**新增/修改 Restful API，根据api code判断，不存在就新增，存在就修改*/
    @PostMapping("/resource/rest/{id}/api")
    @ResponseBody
    public JsonBean<MagicEntity> saveOrUpdateRestfulApi(@PathVariable("id") String id, MagicHttpServletRequest request) throws IOException {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if (entity instanceof RestSiteInfo restSiteInfo) {
            byte[] bytes = IoUtils.bytes(request.getInputStream());
            RestEndpoint endpoint = JsonUtils.readValue(bytes, RestEndpoint.class);
            restSiteInfo.getApis().removeIf(api -> api.getCode().equals(endpoint.getCode()));
            restSiteInfo.addApi(endpoint);
            MagicConfiguration.getMagicResourceService().saveFile(restSiteInfo);
            return new JsonBean<>(restSiteInfo);
        } else {
            return new JsonBean<>(-1, String.format("save fail: current file [%s] is not Restful resource", id));
        }
    }

    /**删除Restful API*/
    @DeleteMapping("/resource/rest/{id}/api/{apiCode}")
    @ResponseBody
    public JsonBean<MagicEntity> deleteRestfulApi(@PathVariable("id") String id, @PathVariable("apiCode") String apiCode) {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if (entity instanceof RestSiteInfo restSiteInfo) {
            boolean removed = restSiteInfo.getApis().removeIf(api -> api.getCode().equals(apiCode));
            if (removed) {
                MagicConfiguration.getMagicResourceService().saveFile(restSiteInfo);
                return new JsonBean<>(restSiteInfo);
            } else {
                return new JsonBean<>(-1, String.format("delete fail: api [%s] not found", apiCode));
            }
        } else {
            return new JsonBean<>(-1, String.format("delete fail: current file [%s] is not Restful resource", id));
        }
    }

    /**如果topic清单中包含就先移除再加上去*/
    private void replaceTopic(List<MessageQueueInfo.TopicInfo> topicInfos, MessageQueueInfo.TopicInfo topicInfo){
        if(topicInfos == null) topicInfos = new ArrayList<>();
        topicInfos.removeIf(topicInfo1 -> topicInfo.getTopicName().equals(topicInfo1.getTopicName()));
        topicInfos.add(topicInfo);
    }

    /**判断topic是否已经存在*/
    private boolean isExistTopic(List<MessageQueueInfo.TopicInfo> topicInfos, MessageQueueInfo.TopicInfo topicInfo){
        if(topicInfos!=null && !topicInfos.isEmpty()){
            return topicInfos.stream().anyMatch(topicInfo1 -> topicInfo.getTopicName().equals(topicInfo1.getTopicName()) );
        }else{
            return false;
        }
    }

    @GetMapping("/resource/file/{id}/sub")
    @ResponseBody
    public JsonBean<Map<String,List<String>>> queryFileStruct(@PathVariable("id") String id, MagicHttpServletRequest request) throws IOException {
        MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
        if(entity instanceof FileInfo fileInfo) {
            Map params = JsonUtils.readValue(IoUtils.bytes(request.getInputStream()),Map.class);
            if(params==null) params = new HashMap();
            String path = CommonUtils.buildFullPath(fileInfo.getRootDir(), !params.containsKey("path") ? "" : String.valueOf(params.get("path")));
            IFileOperator fileOperator = FileOperatorFactory.newFileOperator(fileInfo);
            Map<String,List<String>> result = new HashMap<>();
            result.put("folders", fileOperator.getFileNames(path, IFileOperator.FileType.DIRECTORY));
            result.put("files", fileOperator.getFileNames(path, IFileOperator.FileType.FILE));
            return new JsonBean<>(result);
        }else{
            return new JsonBean<>(-1, String.format("save fail: current file [%s] is not File resource", id));
        }
    }

}
