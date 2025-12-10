package com.iwhalecloud.bss.uba.adapter.datasource;

import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.DynamicModule;
import com.iwhalecloud.bss.magic.script.MagicScriptContext;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.functions.DynamicAttribute;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.iwhalecloud.bss.uba.comm.CommonUtils;
import com.ztesoft.zsmart.core.jdbc.BaseDAO;
import com.ztesoft.zsmart.core.jdbc.JdbcUtil;
import com.ztesoft.zsmart.core.jdbc.ParamArray;
import com.ztesoft.zsmart.core.jdbc.ParamMap;
import com.ztesoft.zsmart.core.jdbc.ds.DbIdentifier;
import com.ztesoft.zsmart.core.jdbc.ds.route.DbRoutingCfg;
import com.ztesoft.zsmart.core.jdbc.ds.route.RoutingManager;
import com.ztesoft.zsmart.core.service.DynamicDict;
import com.ztesoft.zsmart.core.utils.BoHelper;
import com.ztesoft.zsmart.core.utils.StringUtil;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;


/**用来操作当前配置的主数据库数据源，与默认的db模块区别是，这里面可以按照现有支持分库分表，特别是V8这种*/
@MagicModule("masterDb")
public class MasterDBModule implements DynamicAttribute<MasterDBModule, MasterDBModule>, DynamicModule<MasterDBModule> {

    private static UbaLogger logger = UbaLogger.getLogger(MasterDBModule.class);

    //操作数据库的BaseDAO对象缓存，key：prodName_routingId
    private static Map<String, BaseDAO> dbMap = new HashMap<>();

    private BaseDAO baseDao ;

    public MasterDBModule(){

    }

    public MasterDBModule(BaseDAO baseDao){
        this.baseDao = baseDao;
    }

    @Override
    public MasterDBModule getDynamicModule(MagicScriptContext context) {
        String key = context.getString("default");
        return getDynamicAttribute(key);
    }

    //key传入的格式：dbName:routingId
    @Override
    public MasterDBModule getDynamicAttribute(String key) {
        if(key==null || key.isEmpty()){
            key = JdbcUtil.getDbBackService().getDbName();
        }
        String dbName ;
        List<Object> routingIds = new ArrayList<>();
        if(key.contains(":")) {
            String[] keys = key.split(":");
            dbName = keys[0].isEmpty() ? JdbcUtil.getDbBackService().getDbName():keys[0].trim();
            if(keys[1]!=null && !keys[1].isEmpty()){
                routingIds = Arrays.stream(keys[1].split(",")).map(Integer::valueOf).sorted().collect(Collectors.toList());
            }
        } else {
            dbName = key;
        }
        String routingIdStr = routingIds.isEmpty() ? null : routingIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String cacheKey = dbName + (routingIdStr==null ? "" : ":" + routingIdStr);
        System.out.println("===================Dao cache key:"+cacheKey + " key:" + key + " routingIds:" + routingIdStr);
        if(!dbMap.containsKey(cacheKey)){
            try {
                BaseDAO dao = new BaseDAO();
                DbIdentifier dbIdentifier = dbName==null? JdbcUtil.getDbIdentifier() : JdbcUtil.getDbIdentifier(dbName);
                routingIds = getAssignRoutingList(routingIdStr, dbIdentifier);
                if(!routingIds.isEmpty()){
                    dao.initRouting(routingIds);
                }else {
                    dao.initRouting();
                }
                dao.setConnection(JdbcUtil.getDbIdentifier(dbName));
                dbMap.put(cacheKey, dao);
            }catch (Exception e){
                logger.error(String.format("init BaseDAO error: key [%s] ,%s", key, e.getMessage()), e);
                throw new RuntimeException(e);
            }
        }
        return new MasterDBModule(dbMap.get(cacheKey));
    }

    @Comment("开启事务，在执行sql语句之前必须执行这个方法")
    public void beginTrans(@Comment(name = "inherit", value = "是否继承上级事务，如果有就继承，如果没有就新增") boolean inherit){
        SessionHolder.initSession(inherit);
    }

    @Comment("事务提交，如果当前连接最后一次提交，才会做实际提交")
    public void commit(){
        SessionHolder.closeSession(true);
    }

    @Comment("回滚事务，强制回滚事务")
    public void rollback(){
        SessionHolder.closeSession(false);
    }

    @Comment("返回查询对象，所有类型当String处理，如果没有查询到数据，返回null")
    public Map<String,Object> query(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                                    @Comment(name = "params", value = "参数列表") List params){
        try {
            return convertToMap(baseDao.query(sql, getParamArray(params)));
        }catch (Exception e){
            throw new RuntimeException(String.format("query single object error: sql [%s] ", sql),e);
        }
    }

    @Comment("返回查询对象列表，所有类型当String处理，如果没有查询到数据，返回空list")
    public List<Map<String,Object>> queryList(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                                              @Comment(name = "params", value = "参数列表") List params){
        try {
            return convertToMap(baseDao.queryList(sql, getParamArray(params)));
        } catch (Exception e) {
            throw new RuntimeException(String.format("query collection object error: sql [%s] ", sql),e);
        }
    }

    @Comment("返回查询树形结构对象列表，所有类型当String处理，如果没有查询到数据，返回空list")
    public List<Map<String,Object>> queryTree(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                                              @Comment(name = "params", value = "参数列表") List params,
                                              @Comment(name = "keyParentId", value = "父结点字段id") String keyParentId,
                                              @Comment(name = "keyId", value = "主键id") String keyId,
                                              @Comment(name = "topId", value = "根节点id，可为空") String topId,
                                              @Comment(name = "childKey", value = "生成后子节点名称") String childKey){
        try {
            return convertToMap((List<DynamicDict>) baseDao.queryTree(sql, getParamArray(params), keyParentId, keyId, topId, childKey));
        }catch (Exception e){
            throw new RuntimeException(String.format("query tree object error: sql [%s] ", sql),e);
        }
    }

    @Comment("执行更新语句，返回影响的记录数")
    public int executeUpdate(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                             @Comment(name = "params", value = "参数列表") List params){
        try{
            return baseDao.executeUpdate(sql, getParamArray(params));
        }catch (Exception e){
            throw new RuntimeException(String.format("execute update object error: sql [%s] ", sql),e);
        }
    }

    @Comment("执行更新语句，返回影响的记录数")
    public int executeUpdate(@Comment(name = "sql", value = "待执行的sql语句，使用:paramName来做参数占位，通过[]表达可选条件，传入的参数为非空才起效果") String sql,
                             @Comment(name = "params", value = "参数列表") Map<String, Object> params){
        try{
            return baseDao.executeUpdate(sql, getParamMap(params));
        }catch (Exception e){
            throw new RuntimeException(String.format("execute update object error: sql [%s] ", sql),e);
        }
    }

    @Comment("批量执行更新语句，返回影响的记录数数组")
    public int[] executeBatch(@Comment(name = "sql", value = "待执行的sql语句，使用:paramName来做参数占位") String sql,
                              @Comment(name = "params", value = "参数列表，每个值必须都是数组，而且每个key对应的值数组长度都一样")Map<String, Object[]> params){
        try{
            return baseDao.executeBatch(sql, getBatchParamMap(params));
        }catch (Exception e){
            throw new RuntimeException(String.format("execute batch object error: sql [%s] ", sql),e);
        }
    }

    private List<Map<String,Object>> convertToMap(List<DynamicDict> list){
        List<Map<String,Object>> result = new ArrayList<>();
        if(list!=null && !list.isEmpty()){
            list.forEach(item->result.add(convertToMap(item)));
        }
        return result;
    }

    private Map<String, Object> convertToMap(DynamicDict dict){
        if(dict==null) return null;
        Map<String, Object> map = new HashMap<>();
        dict.valueMap.forEach((k,v)->{
            map.put(CommonUtils.getCamelCaseString(k.toLowerCase(), false), v);
        });
        return map;
    }

    private ParamMap getParamMap(Map<String, Object> params){
        ParamMap paramMap = new ParamMap();
        params.forEach(paramMap::set);
        return paramMap;
    }

    private ParamMap getBatchParamMap(Map<String, Object[]> params){
        ParamMap paramMap = new ParamMap();
        params.forEach(paramMap::set);
        return paramMap;
    }

    private ParamArray getParamArray(List args) {
        ParamArray paramArray = new ParamArray();
        if (args != null) {
            for(Object obj : args) {
                Class<?> objClass = obj.getClass();
                if (BoHelper.isSimpleType(objClass)) {
                    if (Long.class.equals(objClass)) {
                        paramArray.set("", (Long)obj);
                    } else if (String.class.equals(objClass)) {
                        paramArray.set("", (String)obj);
                    } else if (java.sql.Date.class.equals(objClass)) {
                        paramArray.set("", (Date)obj);
                    } else if (java.util.Date.class.equals(objClass)) {
                        paramArray.set("", (java.util.Date)obj);
                    } else if (BigDecimal.class.equals(objClass)) {
                        paramArray.set("", (BigDecimal)obj);
                    } else if (Integer.class.equals(objClass)) {
                        paramArray.set("", (Integer)obj);
                    } else {
                        throw new RuntimeException(String.format("business dao do not support this param type: %s" , objClass));
                    }
                }
            }
        }
        return paramArray;
    }

    private List<Object> getAssignRoutingList(String assignRouting, DbIdentifier dbId) {
        // 支持只处理指定路由
        if (StringUtil.isNotEmpty(assignRouting) && dbId != null) {
            String[] temArr = assignRouting.split(",");
            int length = temArr.length;
            Object[] routingIds = new Object[length];
            for (int i = 0; i < length; i++) {
                routingIds[i] = temArr[i].trim();
                // 校验参数有效性 <br>
                DbRoutingCfg.checkRoutingIdExist(routingIds[i]);
            }
            // 去重 <br>
            return RoutingManager.getInstance().filterRouteIdList(dbId, Arrays.asList(routingIds));
        }else {
            // 针对CC/RB/TT去重 <br>
            List<Object> routeIdList = null;
            if (DbRoutingCfg.isDBBackService(dbId)) {
                routeIdList = DbRoutingCfg.getAllCCRouteIdList();
            }else if (DbRoutingCfg.isDBCache(dbId)) {
                routeIdList = DbRoutingCfg.getAllMDBRouteIdList();
            }else if (DbRoutingCfg.isDBBilling(dbId)) {
                routeIdList = DbRoutingCfg.getAllRBRouteIdList();
            }else {
                // DbIdentifier参数为null时遍历所有路由 <br>
                routeIdList = DbRoutingCfg.getAllRouteIdList();
            }
            if (routeIdList!=null && !routeIdList.isEmpty()) {
                return routeIdList;
            }
        }
        List<Object> routingIdList = new ArrayList<Object>();
        routingIdList.add(-1);
        // 返回默认路由 <br>
        return routingIdList;
    }
}
