## 背景与目标

* 在 Magic-Script 中以 `restful.get(...)`、`restful.post(...)` 等方式，按“方法名”调用外部 RESTful 接口。

* 参照 `DubboModule` 的实现，提供“站点”级资源配置、动态注册、模块调用的完整闭环。

## 总体设计

* 新增一个 Magic 插件模块 `RestfulModule`（`@MagicModule("restful")`），支持 `get/post/patch/put/delete` 方法，按“站点 + 接口方法名”进行调用。

* 站点及接口清单作为 Magic 资源（JSON）进行维护，新增 `restful:0` 资源类型；监听文件事件动态注册与注销，运行时形成站点 → 接口映射与调用入口。

* 调用链：Magic 脚本 → `RestfulModule` → 站点操作器 `RestSiteOperator`（封装 `RestTemplate`）→ 外部 RESTful 服务。

## 资源配置模型（Restful 站点）

* 新增实体 `RestSiteInfo extends MagicEntity`，字段：

  * `key`：站点唯一键

  * `host`：域名或协议+域名，如 `https://api.example.com`

  * `port`：端口（可空，默认随协议）

  * `contextPath`：上下文前缀，如 `/v1`

  * `defaultHeaders`：Map（默认头，支持常量或占位符）

  * `apis`：Set<`RestEndpoint`>（接口清单，按 `code` 唯一）

* `RestEndpoint` 字段：

  * `code`：接口方法唯一编码（脚本中作为“方法名”）

  * `name`：接口中文名称

  * `httpMethod`：`GET/POST/PATCH/PUT/DELETE` 等

  * `relativeUrl`：相对 URL（支持路径变量，如 `/users/{id}`）

  * `urlParams`：List\<ParameterInfo>（URL 参数/路径参数定义）

  * `requestBody`：List\<ParameterInfo> 或 JSON Schema（请求体定义）

  * `urlExample`：URL 样例字符串

  * `bodyExample`：请求体样例（JSON）

  * `responseSchema`：返回报文定义（可为 List\<ParameterInfo> 或 JSON Schema）

  * `responseExample`：回参样例（JSON）

* `FieldDef`（参数/请求体通用字段）包含：

  * `name`、`code`、`dataType`（`STRING/INTEGER/LONG/NUMBER/BOOLEAN/DATE/DATETIME/ARRAY/OBJECT`）、`format`（如 `yyyy-MM-dd` 或正则）、`required`（布尔）、`description`。

* 存储：新增 `RestfulMagicResourceStorage implements MagicResourceStorage<RestSiteInfo>`

  * `folder() -> "restful"`、`suffix() -> ".json"`、`allowRoot() -> true`、`requirePath() -> false`、`requiredScript() -> false`

  * `validate(...)` 检查 `key/host` 唯一与合法性，`code` 不重复。

  * 参考：`DubboMagicResourceStorage` 结构与校验方式（uba-rest-component/.../DubboMagicResourceStorage.java:19-78）。

## 动态注册机制

* 新增 `RestfulMagicDynamicRegistry extends AbstractMagicDynamicRegistry<RestSiteInfo>`：

  * 维护 `Map<String, RestSiteOperator> operators`（key → 站点操作器）。

  * 监听 `FileEvent`：`@EventListener(condition = "#event.type == 'restful'")`，调用 `processEvent(event)`。

  * `register(...)`：构建 `RestSiteOperator`（拼装 baseUrl、编译 `relativeUrl` 模板、缓存 `apis`），放入 `operators`。

  * `unregister(...)`：移除 `operators` 对应项。

  * 参考：`DubboMagicDynamicRegistry`（uba-rest-component/.../DubboMagicDynamicRegistry.java:23-58）。

## 调用执行器：RestSiteOperator

* 负责把“方法名”映射到 `RestEndpoint`，并用 `RestTemplate` 发送请求：

  * `invoke(String apiCode, HttpMethod method, Map<String,Object> urlParams, Map<String,Object> queryParams, Object body, Map<String,String> headers)` → `ResponseEntity<String>`（或通用 `Object`）。

  * 解析路径变量 `{var}` 与查询串；`defaultHeaders` 与调用时 headers 合并。

  * 根据 `FieldDef` 做前置校验（`required`、`dataType`、`format`/正则、日期格式等）。

  * 统一异常与日志（参考 DubboModule 的校验与日志风格，uba-rest-component/.../DubboModule.java:83-91）。

## 模块 API 设计（Magic-Script）

* `@MagicModule("restful") public class RestfulModule implements DynamicAttribute<RestfulModule, RestfulModule>, DynamicModule<RestfulModule>`：

  * 站点选择：

    * `getDynamicModule(MagicScriptContext ctx)`：无/多站点时按上下文默认键选择（参考 Dubbo 的默认键逻辑，uba-rest-component/.../DubboModule.java:33-54）。

    * `getDynamicAttribute(String key)`：`restful[key]`/`restful.key` 切换站点（参考 Dubbo，uba-rest-component/.../DubboModule.java:93-101）。

  * 方法族：

    * `String get(String apiCode, Map<String,Object> params)`：按定义生成 URL（含路径变量与查询），无请求体。

    * `String delete(String apiCode, Map<String,Object> params)`：同上。

    * `String post(String apiCode, Object body, Map<String,Object> params)`：JSON 请求体。

    * `String put(String apiCode, Object body, Map<String,Object> params)`：JSON 请求体。

    * `String patch(String apiCode, Object body, Map<String,Object> params)`：JSON 请求体。

    * 可选：`Object call(String apiCode, Map options)`（高级入口，返回 `Map/List/Object`）。

  * 返回：默认返回字符串；提供 `invokeForSimple/Map/List` 风格的变体以便类型友好（参照 Dubbo 的三个变体）。

## 参数与格式校验

* 依据 `FieldDef` 在模块入口做前置校验（缺失必填、类型不匹配、格式不满足直接抛错）。

* 日期/时间按 `format` 解析；正则格式校验优先；数组/对象做结构浅校验（可扩展 JSON Schema 深校验）。

## Bean 与集成

* 在 `UbaIntfConfig` 中新增 Bean：

  * `@Bean RestfulMagicResourceStorage`

  * `@Bean RestfulMagicDynamicRegistry(RestfulMagicResourceStorage)`

  * `@Bean RestfulModule(RestfulMagicDynamicRegistry, RestTemplate)`

  * 如需共用 `RestTemplate`，可抽出一个 `@Bean RestTemplate`，并被现有 `MultiHttpModule` 与 `RestfulModule` 复用（参考现有创建方式，uba-rest-component/.../UbaIntfConfig.java:38-52）。

## Magic 脚本用法示例

* 单站点默认：

  * `res = restful.get("user.get", {id: 123})`

  * `res = restful.post("user.create", {name: "Tom"}, {traceId: "abc"})`

* 多站点选择：

  * `res = restful["userSite"].get("user.get", {id: 123})`

  * `res = restful["orderSite"].post("order.create", body, {force:true})`

* 按 Dubbo 动态属性风格，亦支持 `restful.userSite.get(...)`。

## 资源 JSON 样例（restful:0）

* 站点文件（`restful/userSite.json`）：

  * `key`: "userSite", `host`: "<https://api.example.com>", `port`: 443, `contextPath`: "/v1", `defaultHeaders`: {"Authorization":"Bearer ${env.TOKEN}"}

  * `apis`: \[

    * `{ code:"user.get", name:"获取用户", httpMethod:"GET", relativeUrl:"/users/{id}", urlParams:[{name:"用户ID", code:"id", dataType:"STRING", format:"^[0-9]{1,20}$", required:true}], requestBody:null, urlExample:"/users/123", bodyExample:null, responseSchema:[{name:"用户ID", code:"id", dataType:"INTEGER"}], responseExample:{"id":123,"name":"Tom"} }`
      ]

## 验证与示例

* 在 `magic-api` UI 创建 `restful:0` 资源，录入站点与接口定义；保存后触发文件事件，注册到运行时（同 Dubbo/MQ 机制）。

* 在 `resources/magic-api/api/2模块操作` 增加示例脚本：`2.x restful模块.ms` 展示 `get/post/put/delete/patch` 用法与校验失败案例。

## 兼容与扩展

* 认证：可在站点与接口级定义 `headers`（如 `Authorization`），并支持模板变量（环境变量、请求上下文）。

* 响应解析：提供 `invokeForSimple/Map/List` 变体，默认 JSON。

* 调试日志：按 `ZSmartLogger` 开关输出请求 URL、方法名、参数概要；异常统一封装为模块错误码。

## 参考代码位置

* Dubbo 模块动态选择与默认键逻辑：`uba-rest-component/src/main/java/com/iwhalecloud/bss/uba/rest/module/DubboModule.java:33-54`、动态属性：`:93-101`、入参校验日志：`:83-91`

* Dubbo 资源存储与注册：`uba-rest-component/.../DubboMagicResourceStorage.java:19-78`、`DubboMagicDynamicRegistry.java:23-58`

* HTTP 模块（文件上传增强）：`uba-rest-component/.../MultiHttpModule.java:10-49`

* Bean 配置示例：`uba-rest-component/.../UbaIntfConfig.java:33-52`

## 交付物清单（新增类/修改）

* `com.iwhalecloud.bss.uba.remote.magic.resource.RestSiteInfo.java`

* `com.iwhalecloud.bss.uba.remote.magic.resource.RestEndpoint.java`

* `com.iwhalecloud.bss.uba.remote.magic.resource.RestfulMagicResourceStorage.java`

* `com.iwhalecloud.bss.uba.remote.magic.resource.RestfulMagicDynamicRegistry.java`

* `com.iwhalecloud.bss.uba.remote.module.RestSiteOperator.java`

* `com.iwhalecloud.bss.uba.remote.module.RestfulModule.java`

* 修改 `UbaIntfConfig`：注册上述 Bean，必要时抽取共享 `RestTemplate`。

* 可选：在 `CommonConst` 增加 `restDefaultSiteCode` 常量，用于默认站点选择。

## 下一步

* 若确认方案，开始按以上清单实现类与配置，补充脚本示例与最小集成验证（单站点+GET/POST）。

