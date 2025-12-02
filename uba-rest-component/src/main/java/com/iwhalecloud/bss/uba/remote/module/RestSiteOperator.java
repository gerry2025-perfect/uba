package com.iwhalecloud.bss.uba.remote.module;

import com.iwhalecloud.bss.uba.common.dto.ParameterInfo;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.iwhalecloud.bss.uba.remote.magic.resource.RestEndpoint;
import com.iwhalecloud.bss.uba.remote.magic.resource.RestSiteInfo;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**Restful站点操作类*/
public class RestSiteOperator {

    private static final UbaLogger logger = UbaLogger.getLogger(RestSiteOperator.class);

    private final RestTemplate restTemplate;

    //private final String baseUrl;
    private final Map<String, RestEndpoint> endpointMap = new HashMap<>();
    private final Map<String, String> defaultHeaders;
    private final RestSiteInfo restSiteInfo;

    public RestSiteOperator(RestSiteInfo siteInfo, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restSiteInfo = siteInfo;
        //this.baseUrl = buildBaseUrl();
        this.defaultHeaders = siteInfo.getDefaultHeaders() == null ? Collections.emptyMap() : siteInfo.getDefaultHeaders();
        if (siteInfo.getApis() != null) {
            siteInfo.getApis().forEach(api -> endpointMap.put(api.getCode(), api));
        }
    }

    public String get(String apiCode, Map<String, Object> body, Map<String, Object> params, Map<String, String> headers) {
        return invoke(apiCode, HttpMethod.GET, params, headers, body, MediaType.APPLICATION_JSON, String.class);
    }

    public String delete(String apiCode, Map<String, Object> params, Map<String, String> headers) {
        return invoke(apiCode, HttpMethod.DELETE, params, headers, null, MediaType.APPLICATION_JSON, String.class);
    }

    public String post(String apiCode, Map<String, Object> body, Map<String, Object> params, Map<String, String> headers) {
        return invoke(apiCode, HttpMethod.POST, params, headers, body, MediaType.APPLICATION_JSON, String.class);
    }

    public String put(String apiCode, Map<String, Object> body, Map<String, Object> params, Map<String, String> headers) {
        return invoke(apiCode, HttpMethod.PUT, params, headers, body, MediaType.APPLICATION_JSON, String.class);
    }

    public String patch(String apiCode, Map<String, Object> body, Map<String, Object> params, Map<String, String> headers) {
        return invoke(apiCode, HttpMethod.PATCH, params, headers, body, MediaType.APPLICATION_JSON, String.class);
    }

    public <T> T invoke(String apiCode, HttpMethod method, Map<String, Object> params, Map<String, String> header, Map<String, Object> body, MediaType contentType, Class<T> cls) {
        RestEndpoint endpoint = endpointMap.get(apiCode);
        if (endpoint == null) {
            throw new RuntimeException("cannot find restful endpoint by code:" + apiCode);
        }
        if(method!=null){
            //如果传入了method，需要校验传入的和endpoint中配置的是否一致
            if(!method.name().equalsIgnoreCase(endpoint.getHttpMethod())){
                throw new RuntimeException(String.format("The http method is not match, input value %s PK endpoint value %s" , method.name(), endpoint.getHttpMethod()));
            }
        }else {
            //如果没有传入，就从endpoint中获取
            method = getHttpMethod(endpoint);
        }
        Map<String, Object> safeParams = params == null ? new HashMap<>() : new HashMap<>(params);
        validateInput(endpoint.getPlaceholderParams(), safeParams, "url placeholder");
        validateInput(endpoint.getUrlParams(), safeParams, "url param");
        if (method != HttpMethod.GET && method != HttpMethod.DELETE) {
            validateInput(endpoint.getRequestSchema(), body, "body");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(defaultHeaders);
        if (contentType != null) {
            headers.setContentType(contentType);
        }
        headers.setAccept(Collections.singletonList(contentType));
        if(header!=null && !header.isEmpty()){
            header.forEach(headers::set);
        }
        String url = buildUrl(endpoint.getRelativeUrl(), safeParams);
        HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("invoke restful %s %s, params:%s", method.name(), url, summarizeParams(safeParams)));
        }
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, cls);
        if(HttpStatus.OK.equals(response.getStatusCode())){
            return response.getBody();
        }else{
            throw new RuntimeException(String.format("invoke restful request fail, status code: %s" ,response.getStatusCode()));
        }

    }

    /**从endpoint中获取对应http method*/
    private HttpMethod getHttpMethod(RestEndpoint endpoint) {
        String httpMethod = endpoint.getHttpMethod();
        if(httpMethod.equalsIgnoreCase("GET")){
            return HttpMethod.GET;
        }else if(httpMethod.equalsIgnoreCase("POST")){
            return HttpMethod.POST;
        }else if(httpMethod.equalsIgnoreCase("PUT")){
            return HttpMethod.PUT;
        }else if(httpMethod.equalsIgnoreCase("PATCH")){
            return HttpMethod.PATCH;
        }else if(httpMethod.equalsIgnoreCase("DELETE")){
            return HttpMethod.DELETE;
        }else{
            throw new RuntimeException(String.format("unsupported http method %s, apiCode: %s", httpMethod, endpoint.getCode()));
        }
    }

    private void validateInput(List<ParameterInfo> defs, Map<String, Object> params, String validateType) {
        if (defs == null || defs.isEmpty()) {
            return;
        }
        for (ParameterInfo def : defs) {
            Object value = params.get(def.getName());
            if (def.isRequired() && (value == null || String.valueOf(value).isEmpty())) {
                throw new RuntimeException(String.format("required %s missing: %s", validateType, def.getName()));
            }
            if (value != null) {
                if (def.getExpression() != null && !def.getExpression().isEmpty() && isRegex(def.getExpression())) {
                    Pattern p = Pattern.compile(def.getExpression());
                    if (!p.matcher(String.valueOf(value)).matches()) {
                        throw new RuntimeException(String.format("%s format not match: %s", validateType, def.getName()));
                    }
                }
            }
        }
    }

    private String buildUrl(String relative, Map<String, Object> params) {
        String path = relative == null ? "" : relative;
        Set<String> pathVars = extractPathVars(path);
        for (String var : pathVars) {
            Object v = params.remove(var);
            if (v == null) {
                throw new RuntimeException("missing path variable:" + var);
            }
            path = path.replace("{" + var + "}", urlEncode(String.valueOf(v)));
        }
        String query = buildQuery(params);
        String full = normalizeBase(buildBaseUrl()) + normalizePath(path);
        return query.isEmpty() ? full : full + "?" + query;
    }

    private String buildBaseUrl() {
        StringBuilder sb = new StringBuilder();
        String host = restSiteInfo.getHost();
        if (!StringUtils.hasText(host)) {
            throw new RuntimeException("host is required");
        }
        sb.append(host);
        if (restSiteInfo.getPort() != null && restSiteInfo.getPort() > 0/* && !host.matches(".*://.*(:\\d+)?$")*/) {
            sb.append(":" + restSiteInfo.getPort());
        }
        String ctx = restSiteInfo.getContextPath();
        if (StringUtils.hasText(ctx)) {
            if (!ctx.startsWith("/")) {
                sb.append("/");
            }
            sb.append(ctx);
        }
        return sb.toString();
    }

    private String normalizeBase(String base) {
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private String normalizePath(String path) {
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }

    private Set<String> extractPathVars(String path) {
        Set<String> vars = new HashSet<>();
        int i = 0;
        while (i < path.length()) {
            int s = path.indexOf('{', i);
            if (s < 0) break;
            int e = path.indexOf('}', s);
            if (e < 0) break;
            String name = path.substring(s + 1, e);
            if (!name.isEmpty()) vars.add(name);
            i = e + 1;
        }
        return vars;
    }

    private String buildQuery(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "";
        return params.entrySet().stream()
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(String.valueOf(e.getValue())))
                .collect(Collectors.joining("&"));
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private boolean isRegex(String format) {
        return format.startsWith("^") || format.contains("[") || format.contains("(");
    }

    private String summarizeParams(Map<String, Object> params) {
        if (params == null) return "";
        return params.keySet().stream().limit(10).collect(Collectors.joining(","));
    }
}