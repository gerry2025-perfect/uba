package com.iwhalecloud.bss.uba.common.magic;

import com.iwhalecloud.bss.magic.magicapi.core.resource.DatabaseResource;
import com.iwhalecloud.bss.magic.magicapi.core.resource.KeyValueResource;
import com.iwhalecloud.bss.magic.magicapi.core.resource.Resource;
import com.iwhalecloud.bss.magic.magicapi.utils.Assert;
import com.iwhalecloud.bss.magic.magicapi.utils.IoUtils;
import com.iwhalecloud.bss.uba.adapter.comm.UbaContext;
import com.iwhalecloud.bss.uba.adapter.datasource.SessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据库资源存储，针对V8需要做
 *
 */
public class UbaDatabaseResource extends DatabaseResource {

	private static final Logger logger = LoggerFactory.getLogger(UbaDatabaseResource.class);
	private final JdbcTemplate template;
	private final String tableName;
	private Map<String, String> cachedContent = new ConcurrentHashMap<>();

	public UbaDatabaseResource(JdbcTemplate template, String tableName) {
		this(template, tableName, false);
	}

	public UbaDatabaseResource(JdbcTemplate template, String tableName, boolean readonly) {
		this(template, tableName, "/magic-api", readonly);
	}

	public UbaDatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly) {
		this(template, tableName, path, readonly, null);
	}

	public UbaDatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly, KeyValueResource parent) {
		super(template, tableName, path, readonly, parent);
		this.template = template;
		this.tableName = tableName;
	}

	public UbaDatabaseResource(JdbcTemplate template, String tableName, String path, boolean readonly, Map<String, String> cachedContent, KeyValueResource parent) {
		this(template, tableName, path, readonly, parent);
		this.cachedContent = cachedContent;
	}

	@Override
	public byte[] read() {
		try {
			initDSSession();
			return super.read();
		}finally {
			closeDSSession();
		}
	}

	@Override
	public void readAll() {
		try {
			initDSSession();
			super.readAll();
		}finally {
			closeDSSession();
		}
	}

	@Override
	public boolean exists() {
		try {
			initDSSession();
			return super.exists();
		}finally {
			closeDSSession();
		}
	}

	@Override
	public boolean write(String content) {
		try {
			initDSSession();
			return super.write(content);
		}finally {
			closeDSSession();
		}
	}

	@Override
	public Set<String> keys() {
		try{
			initDSSession();
			return super.keys();
		}finally {
			closeDSSession();
		}
	}

	@Override
	public boolean renameTo(Map<String, String> renameKeys) {
		try {
			initDSSession();
			return super.renameTo(renameKeys);
		}finally {
			closeDSSession();
		}
	}

	@Override
	public boolean delete() {
		try {
			initDSSession();
			return super.delete();
		}finally {
			closeDSSession();
		}
	}

	@Override
	public Function<String, Resource> mappedFunction() {
		return it -> new UbaDatabaseResource(template, tableName, it, readonly, this.cachedContent, this);
	}

	/**初始化数据库会话*/
	private void initDSSession(){
		if(UbaContext.isV8()){
			SessionHolder.initSession(true);
		}
	}

	/**关闭数据库会话*/
	private void closeDSSession(){
		if(UbaContext.isV8()){
			SessionHolder.closeSession(true);
		}
	}

}
