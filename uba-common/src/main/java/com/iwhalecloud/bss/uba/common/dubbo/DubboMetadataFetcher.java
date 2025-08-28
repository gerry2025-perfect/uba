package com.iwhalecloud.bss.uba.common.dubbo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.shaded.org.apache.curator.framework.CuratorFramework;
import org.apache.dubbo.shaded.org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.dubbo.shaded.org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**从zk服务器上获取已注册的服务*/
public class DubboMetadataFetcher implements AutoCloseable {

    private static final String ROOT_PATH = "/dubbo"; // Dubbo 2.x 根路径
    private final CuratorFramework zkClient;

    public DubboMetadataFetcher(String zkAddress) {
        this.zkClient = CuratorFrameworkFactory.newClient(
                zkAddress,
                60000,
                30000,
                new ExponentialBackoffRetry(1000, 3)
        );
        this.zkClient.start();
    }

    private String getGroupName(String groupName){
        if(groupName==null || "".equals(groupName)){
            groupName = ROOT_PATH;
        }else if(!groupName.startsWith("/")){
            groupName = "/" + groupName;
        }
        return groupName;
    }

    public Map<String,Map<String,List<MethodInfo>>> getServiceMethods(String groupName) throws Exception {
        groupName = getGroupName(groupName);
        Map<String,Map<String,List<MethodInfo>>> result = new HashMap();
        if (zkClient.checkExists().forPath(groupName) != null) {
            List<String> classList = zkClient.getChildren().forPath(groupName);
            String finalGroupName = groupName;
            classList.stream().forEach(className -> {
                try {
                    result.put(className, getServiceMethods(finalGroupName, className));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return result;
    }

    /**
     * 获取服务的所有方法及其参数信息
     */
    public Map<String, List<MethodInfo>> getServiceMethods(String groupName, String serviceName) throws Exception {
        Map<String, List<MethodInfo>> methodMap = new HashMap<>();
        groupName = getGroupName(groupName);
        String providersPath = String.format("%s/%s/providers", groupName, serviceName);
        System.out.println("查询Path：" + providersPath);
        if (zkClient.checkExists().forPath(providersPath) != null) {
            List<String> providerNodes = zkClient.getChildren().forPath(providersPath);
            for (String node : providerNodes) {
                if(node.startsWith("dubbo")){
                    node = "http" + node.substring("dubbo".length());
                }
                // 解码 URL
                String encodedUrl = node ; //new String(zkClient.getData().forPath(providersPath + "/" + node));
                String urlStr = URL.decode(encodedUrl);
                URL url = URL.valueOf(urlStr);
                System.out.println("URL地址：" + urlStr);
                // 解析 methods 参数
                String methodsParam = url.getParameter("methods");
                if (methodsParam != null && !methodsParam.isEmpty()) {
                    String[] methodSignatures = methodsParam.split(",");

                    for (String signature : methodSignatures) {
                        MethodInfo methodInfo = parseMethodSignature(signature);
                        methodMap.computeIfAbsent(methodInfo.getName(), k -> new ArrayList<>())
                                .add(methodInfo);
                    }
                }
            }
        }
        return methodMap;
    }

    /**
     * 解析方法签名（例如："addUser(java.lang.String,java.lang.Integer)"）
     */
    private MethodInfo parseMethodSignature(String signature) {
        MethodInfo methodInfo = new MethodInfo();
        if(!signature.contains("(")) {
            methodInfo.setName(signature);
        }else {
            // 匹配方法名
            Pattern methodPattern = Pattern.compile("([a-zA-Z_$][a-zA-Z0-9_$]*)\\((.*)\\)");
            Matcher methodMatcher = methodPattern.matcher(signature);
            if (methodMatcher.find()) {
                methodInfo.setName(methodMatcher.group(1));
                // 解析参数类型
                String paramTypesStr = methodMatcher.group(2);
                if (!paramTypesStr.isEmpty()) {
                    String[] paramTypes = paramTypesStr.split(",");
                    for (String paramType : paramTypes) {
                        methodInfo.addParameter(paramType.trim());
                    }
                }
            }
        }
        return methodInfo;
    }

    public void close() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    // 方法信息类
    public static class MethodInfo {
        private String name;
        private List<String> parameterTypes = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getParameterTypes() {
            return parameterTypes;
        }

        public void addParameter(String paramType) {
            this.parameterTypes.add(paramType);
        }

        @Override
        public String toString() {
            return name + "(" + String.join(", ", parameterTypes) + ")";
        }
    }

    public static void main(String[] args) {
        try (DubboMetadataFetcher fetcher = new DubboMetadataFetcher("10.10.168.153:10456")) {
            String serviceName = "com.iwhalecloud.bss.cpc.service.api.inf.agreement.AgreementQueryService";
            /*Map<String, Map<String, List<MethodInfo>>> serviceMethods = fetcher.getServiceMethods("CPC_OUT");
            serviceMethods.entrySet().stream().forEach(entry -> {
                entry.getValue().entrySet().stream().forEach(methodEntry -> {
                    System.out.println(entry.getKey() + "." + methodEntry.getKey());
                });
            });*/
            Map<String, List<MethodInfo>> methods = fetcher.getServiceMethods("CPC_OUT", serviceName);
            System.out.println("服务 " + serviceName + " 的方法列表：");
            methods.forEach((methodName, methodInfos) -> {
                System.out.println("\n方法: " + methodName);
                methodInfos.forEach(info -> {
                    System.out.println("  参数: " + String.join(", ", info.getParameterTypes()));
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
