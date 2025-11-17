package com.iwhalecloud.bss.uba.remote.magic.resource;

import com.iwhalecloud.bss.uba.common.dto.ParameterInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RestEndpoint {
    private String code;
    private String name;
    private String httpMethod;
    private String relativeUrl;
    /**在url上占位符参数，比如：url为/ctx/resource/{id}，其中id就是一个参数*/
    private List<ParameterInfo> placeholderParams = new ArrayList<>();
    /**这里面定义url请求上拼的参数，比如：/ctx/resource/{id}?param1=11&param2=12，其中param1和param2*/
    private List<ParameterInfo> urlParams = new ArrayList<>();
    /**http head中的参数*/
    private List<ParameterInfo> headers = new ArrayList<>();
    /**请求体的参数定义*/
    private List<ParameterInfo> requestSchema = new ArrayList<>();
    private String urlExample;
    private Object bodyExample;
    /**返回体的参数定义*/
    private List<ParameterInfo> responseSchema = new ArrayList<>();
    private String responseExample;
}