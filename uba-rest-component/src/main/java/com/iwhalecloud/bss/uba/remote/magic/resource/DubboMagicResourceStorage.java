package com.iwhalecloud.bss.uba.remote.magic.resource;

import com.iwhalecloud.bss.magic.magicapi.core.model.JsonCode;
import com.iwhalecloud.bss.magic.magicapi.core.model.MagicEntity;
import com.iwhalecloud.bss.magic.magicapi.core.service.MagicResourceService;
import com.iwhalecloud.bss.magic.magicapi.utils.IoUtils;
import com.iwhalecloud.bss.magic.magicapi.utils.JsonUtils;
import com.iwhalecloud.bss.uba.common.magic.PlaceholderResourceStorage;

import java.util.Objects;

/**获取的信息，就是json文件中groupId=dubbo:0*/
public class DubboMagicResourceStorage extends PlaceholderResourceStorage<DubboInfo> {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "dubbo";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<DubboInfo> magicClass() {
		return DubboInfo.class;
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
	public String buildMappingKey(DubboInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(DubboInfo entity) {
		notBlank(entity.getRegisterAddr(), new JsonCode(2001, "register address is required"));
		notBlank(entity.getKey(), new JsonCode(2001, "key is required"));
		isTrue(IoUtils.validateFileName(entity.getKey()), new JsonCode(1026, "dubbo-Key cannot contain special characters"));
		boolean noneMatchKey = magicResourceService.listFiles("dubbo:0").stream()
				.map(it -> (DubboInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, new JsonCode(1022, "dubbo-Key is exists"));
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	/*@Override
	public DubboInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, DubboInfo.class);
	}*/

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
