package com.iwhalecloud.bss.uba.common.log;

/**
 * 日志持久化策略接口 (Strategy Pattern).
 * 允许自定义日志的最终写入方式，例如写入文件、数据库、Kafka等.
 */
@FunctionalInterface
public interface LogPersistenceStrategy {

    void persist(Object logData);

}