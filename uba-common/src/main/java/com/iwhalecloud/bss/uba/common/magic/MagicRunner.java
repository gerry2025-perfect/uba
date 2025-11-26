package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.uba.common.CommonUtils;
import com.iwhalecloud.bss.uba.common.ResultEntity;
import com.iwhalecloud.bss.uba.common.exception.ExceptionDefine;
import com.iwhalecloud.bss.uba.common.prop.PropertyHolder;
import com.ztesoft.zsmart.core.log.ZSmartLogger;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicAPIService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**magic脚本执行器，对magic-api进行包装、执行*/
public class MagicRunner {

    private static final ZSmartLogger logger = ZSmartLogger.getLogger(MagicRunner.class);

    private final MagicAPIService magicAPIService;

    public MagicRunner(MagicAPIService magicAPIService){
        this.magicAPIService = magicAPIService;
    }

    public ResultEntity executeMagicApi(String url, Map<String,Object> params){
        if(logger.isDebugEnabled()){
            logger.debug(String.format("execute magic api url: %s, params: %s ", url, CommonUtils.convertToString(params)));
        }
        Object result = magicAPIService.execute("POST",url,params);
        if(logger.isDebugEnabled()){
            logger.debug(String.format("magic api result: %s", CommonUtils.convertToString(result)));
        }
        if(result instanceof ResultEntity){
            return (ResultEntity)result;
        }else{
            logger.error("magic api result must be of type ResultEntity , current type is " + result.getClass());
            return ResultEntity.failResult(ExceptionDefine.MQ_ERROR_TYPE_OPERATE_FAIL.getErrorCode(),
                    ExceptionDefine.MQ_ERROR_TYPE_OPERATE_FAIL.getErrorMsg() + ", magic api result must be of type ResultEntity , current type is %s", result.getClass());
        }

    }

}
