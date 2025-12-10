package com.iwhalecloud.bss.uba.adapter.datasource;

import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@MagicModule("masterDb")
public class MasterDBModule {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    // Use ThreadLocal to manage transaction status for manual transaction control
    private static final ThreadLocal<TransactionStatus> transactionStatusThreadLocal = new ThreadLocal<>();

    public MasterDBModule(DataSource dataSource, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionManager = transactionManager;
    }

    @Comment("手动开启事务，如果当前操作中只有查询，可以不需要事务")
    public void beginTrans(@Comment(name = "inherit", value = "是否继承上级事务，如果有就继承，如果没有就新增") boolean inherit) {
        if (transactionStatusThreadLocal.get() == null) {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(inherit ? TransactionDefinition.PROPAGATION_SUPPORTS : TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            transactionStatusThreadLocal.set(transactionManager.getTransaction(def));
        }
    }

    @Comment("事务提交，如果当前连接最后一次提交，才会做实际提交")
    public void commit() {
        TransactionStatus status = transactionStatusThreadLocal.get();
        if (status != null && !status.isCompleted()) {
            transactionManager.commit(status);
        }
        transactionStatusThreadLocal.remove();
    }

    @Comment("回滚事务，强制回滚事务")
    public void rollback() {
        TransactionStatus status = transactionStatusThreadLocal.get();
        if (status != null && !status.isCompleted()) {
            transactionManager.rollback(status);
        }
        transactionStatusThreadLocal.remove();
    }

    @Comment("返回查询对象，所有类型当String处理，如果没有查询到数据，返回null")
    public Map<String, Object> query(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                                     @Comment(name = "params", value = "参数列表") List<Object> params) {
        return jdbcTemplate.queryForMap(sql, params != null ? params.toArray() : null);
    }

    @Comment("返回查询对象列表，所有类型当String处理，如果没有查询到数据，返回空list")
    public List<Map<String, Object>> queryList(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                                               @Comment(name = "params", value = "参数列表") List<Object> params) {
        return jdbcTemplate.queryForList(sql, params != null ? params.toArray() : null);
    }

    @Comment("执行更新语句，返回影响的记录数")
    public int executeUpdate(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                             @Comment(name = "params", value = "参数列表") List<Object> params) {
        return jdbcTemplate.update(sql, params != null ? params.toArray() : null);
    }

    @Comment("批量执行更新语句，返回影响的记录数数组")
    public int[] executeBatch(@Comment(name = "sql", value = "待执行的sql语句，使用问号作为参数占位符") String sql,
                              @Comment(name = "params", value = "参数列表，每个元素是一个包含单次执行参数的List") List<Object[]> params) {
        return jdbcTemplate.batchUpdate(sql, params);
    }
}
