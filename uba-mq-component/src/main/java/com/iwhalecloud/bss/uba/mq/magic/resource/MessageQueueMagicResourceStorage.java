package com.iwhalecloud.bss.uba.mq.magic.resource;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.util.Objects;

/**获取的信息，就是json文件中groupId=mq:0*/
public class MessageQueueMagicResourceStorage implements MagicResourceStorage<MessageQueueInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "mq";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<MessageQueueInfo> magicClass() {
		return MessageQueueInfo.class;
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
	public String buildMappingKey(MessageQueueInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(MessageQueueInfo entity) {
		notBlank(entity.getNamesrvAddr(), new JsonCode(2001, "namesrvAddr is required"));
		notBlank(entity.getKey(), new JsonCode(2001, "key is required"));
		isTrue(IoUtils.validateFileName(entity.getKey()), new JsonCode(1026, "messageQueue-Key cannot contain special characters"));
		boolean noneMatchKey = magicResourceService.listFiles("mq:0").stream()
				.map(it -> (MessageQueueInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, new JsonCode(1022, "messageQueue-Key is exists"));
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public MessageQueueInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, MessageQueueInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
