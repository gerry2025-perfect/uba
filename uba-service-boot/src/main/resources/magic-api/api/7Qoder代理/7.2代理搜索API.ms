{
  "id": "qoder_proxy_search_api",
  "name": "Qoder代理搜索API",
  "paths": ["/qoder-proxy/search"],
  "methods": ["GET"],
  "parameters": [
    {
      "name": "query",
      "value": "",
      "description": "搜索关键词",
      "required": true,
      "dataType": "String"
    },
    {
      "name": "method",
      "value": "smart",
      "description": "通信方式：smart|api|ipc|cli",
      "required": false,
      "dataType": "String"
    },
    {
      "name": "limit",
      "value": "10",
      "description": "结果数量限制",
      "required": false,
      "dataType": "Integer"
    }
  ],
  "options": [],
  "requestBody": "",
  "headers": [],
  "description": "通过代理访问Qoder IDE索引的搜索API",
  "requestBodyDefinition": null,
  "responseBodyDefinition": null
}
================================
import qoderProxy;

var query = request.query;
var method = request.method || 'smart';
var limit = parseInt(request.limit || '10');

if (!query) {
    return {
        "code": 400,
        "message": "搜索关键词不能为空",
        "data": null,
        "success": false
    };
}

var result;
var usedMethod = method;

switch (method.toLowerCase()) {
    case 'api':
        result = qoderProxy.searchViaApi(query, limit);
        break;
    case 'ipc':
        result = qoderProxy.searchViaIPC(query, limit);
        break;
    case 'cli':
        result = qoderProxy.searchViaCLI(query, limit);
        break;
    case 'smart':
    default:
        result = qoderProxy.smartSearch(query, limit);
        usedMethod = 'smart';
        break;
}

// 增强返回结果
if (result && result.success && result.data) {
    var enhancedData = result.data.map(item => {
        return {
            "path": item.path || "",
            "filename": item.filename || "",
            "extension": item.extension || "",
            "size": item.size || 0,
            "score": item.score || 0,
            "snippet": item.snippet || "",
            "relativePath": getRelativePath(item.path),
            "fileType": getFileTypeDescription(item.extension || ""),
            "lastModified": item.lastModified || ""
        };
    });
    
    return {
        "code": 1,
        "message": "搜索完成",
        "data": {
            "query": query,
            "method": usedMethod,
            "totalResults": enhancedData.length,
            "results": enhancedData,
            "qoderIndexStatus": "connected"
        },
        "success": true,
        "timestamp": new Date()
    };
}

// 如果搜索失败，返回错误信息
return {
    "code": result ? result.code || 0 : 0,
    "message": result ? result.message || "搜索失败" : "搜索失败",
    "data": {
        "query": query,
        "method": usedMethod,
        "totalResults": 0,
        "results": [],
        "qoderIndexStatus": "disconnected"
    },
    "success": false,
    "timestamp": new Date()
};

function getRelativePath(fullPath) {
    if (!fullPath) return "";
    return fullPath.replace(/^.*[\\\/]uba93-product[\\\/]/, '');
}

function getFileTypeDescription(extension) {
    var typeMap = {
        "java": "Java源文件",
        "xml": "XML配置文件", 
        "properties": "属性配置文件",
        "yml": "YAML配置文件",
        "yaml": "YAML配置文件",
        "json": "JSON文件",
        "js": "JavaScript文件",
        "ts": "TypeScript文件",
        "css": "样式文件",
        "html": "HTML文件",
        "md": "Markdown文档",
        "txt": "文本文件",
        "sql": "SQL脚本",
        "ms": "Magic Script文件"
    };
    return typeMap[extension] || "未知类型";
}