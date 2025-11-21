package com.iwhalecloud.bss.uba.file.magic.resource;

import org.springframework.stereotype.Component;
import com.iwhalecloud.bss.magic.magicapi.core.config.JsonCodeConstants;
import com.iwhalecloud.bss.magic.magicapi.core.model.JsonCode;
import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceService;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceStorage;
import com.iwhalecloud.bss.magic.magicapi.utils.IoUtils;
import com.iwhalecloud.bss.magic.magicapi.utils.JsonUtils;

import java.util.Objects;

@Component
/**获取的信息，就是json文件中groupId=file:0*/
public class FileMagicResourceStorage implements MagicResourceStorage<FileInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "file";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<FileInfo> magicClass() {
		return FileInfo.class;
	}

	@Override
	public boolean requirePath() {
		return false;
	}

	@Override
	public boolean requiredScript() {
		return false;
	}

	@Override
	public boolean allowRoot() {
		return true;
	}

	@Override
	public String buildMappingKey(FileInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(FileInfo entity) {
		notBlank(entity.getRootDir(), new JsonCode(2001, "root directory is required"));
		notBlank(entity.getKey(), new JsonCode(2001, "key is required"));
		isTrue(IoUtils.validateFileName(entity.getKey()), new JsonCode(1026, "file-Key cannot contain special characters"));
		boolean noneMatchKey = magicResourceService.listFiles("file:0").stream()
				.map(it -> (FileInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, new JsonCode(1022, "file-Key is exists"));
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public FileInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, FileInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
