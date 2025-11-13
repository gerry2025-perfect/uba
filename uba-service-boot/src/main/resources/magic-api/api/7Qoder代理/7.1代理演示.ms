{
  "id": "qoder_proxy_demo",
  "name": "Qoder代理演示",
  "paths": ["/qoder-proxy/demo"],
  "methods": ["GET"],
  "parameters": [],
  "options": [],
  "requestBody": "",
  "headers": [],
  "description": "演示如何使用Qoder IDE现有索引能力",
  "requestBodyDefinition": null,
  "responseBodyDefinition": null
}
================================
// 演示通过代理访问Qoder IDE索引功能
import qoderProxy;

log.info("开始演示Qoder IDE索引代理功能");

// 1. 获取Qoder IDE索引状态
var statusResult = qoderProxy.getIndexStatus();
log.info("Qoder IDE索引状态: {}", statusResult);

// 2. 智能搜索 - 自动选择最佳通信方式
var smartSearchResult = qoderProxy.smartSearch("CommonUtils", 5);
log.info("智能搜索结果: {}", smartSearchResult);

// 3. 搜索Java类
var classSearchResult = qoderProxy.searchJavaClasses("DubboOperator", 3);
log.info("Java类搜索结果: {}", classSearchResult);

// 4. 搜索方法
var methodSearchResult = qoderProxy.searchMethods("getInstance", 5);
log.info("方法搜索结果: {}", methodSearchResult);

// 5. 搜索配置项
var configSearchResult = qoderProxy.searchConfigs("dubbo.registry", 5);
log.info("配置搜索结果: {}", configSearchResult);

// 6. 尝试不同的通信方式
var apiResult = null;
var ipcResult = null;
var cliResult = null;

try {
    apiResult = qoderProxy.searchViaApi("magic-api", 3);
    log.info("API搜索结果: {}", apiResult);
} catch (e) {
    log.warn("API搜索失败: {}", e.message);
}

try {
    ipcResult = qoderProxy.searchViaIPC("Spring", 3);
    log.info("IPC搜索结果: {}", ipcResult);
} catch (e) {
    log.warn("IPC搜索失败: {}", e.message);
}

try {
    cliResult = qoderProxy.searchViaCLI("Configuration", 3);
    log.info("CLI搜索结果: {}", cliResult);
} catch (e) {
    log.warn("CLI搜索失败: {}", e.message);
}

return {
    "message": "Qoder IDE索引代理演示完成",
    "timestamp": new Date(),
    "results": {
        "indexStatus": statusResult,
        "smartSearch": smartSearchResult,
        "classSearch": classSearchResult,
        "methodSearch": methodSearchResult,
        "configSearch": configSearchResult,
        "communicationMethods": {
            "api": apiResult ? apiResult.success : false,
            "ipc": ipcResult ? ipcResult.success : false,
            "cli": cliResult ? cliResult.success : false
        }
    },
    "summary": {
        "totalSearches": 6,
        "availableMethods": [
            apiResult ? "API" : null,
            ipcResult ? "IPC" : null, 
            cliResult ? "CLI" : null
        ].filter(method => method !== null)
    }
};