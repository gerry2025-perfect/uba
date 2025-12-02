package com.iwhalecloud.bss.uba.adapter.datasource;

import com.iwhalecloud.bss.uba.adapter.comm.UbaContext;
import com.iwhalecloud.bss.uba.adapter.log.UbaLogger;
import com.ztesoft.zsmart.core.exception.BaseAppException;
import com.ztesoft.zsmart.core.jdbc.JdbcUtil;
import com.ztesoft.zsmart.core.jdbc.SessionContext;
import com.ztesoft.zsmart.core.jdbc.ses.trans.TransactionHolder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class UbaAdapterDataSource implements DataSource {

    private final static UbaLogger logger = UbaLogger.getLogger(UbaAdapterDataSource.class);

    @Override
    public Connection getConnection() throws SQLException {
        try {
            logger.debug(String.format("getConnection for [%s]",UbaContext.getDbName()==null?"default" : UbaContext.getDbName()));
            if(UbaContext.getDbName()==null){
                return JdbcUtil.getConnection();
            }else {
                return JdbcUtil.getConnection(UbaContext.getDbName());
            }
        } catch (BaseAppException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
