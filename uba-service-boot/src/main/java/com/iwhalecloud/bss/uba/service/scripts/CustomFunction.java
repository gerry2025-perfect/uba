package com.iwhalecloud.bss.uba.service.scripts;

import com.iwhalecloud.bss.magic.magicapi.core.config.MagicFunction;
import com.iwhalecloud.bss.magic.script.annotation.Comment;
import com.iwhalecloud.bss.magic.script.annotation.Function;
import com.iwhalecloud.bss.magic.script.functions.DateExtension;

import java.util.Date;

/**
 * 自定义函数
 * https://ssssssss.org/magic-api/pages/senior/function/
 */
public class CustomFunction implements MagicFunction {

	// 脚本中直接使用 now();
	@Function
	@Comment("Get current time")
	public Date now() {
		return new Date();
	}

	// 脚本中使用 date_format(now())
	@Function
	@Comment("Date formatting")
	public String date_format(@Comment(name = "target", value = "Target date") Date target) {
		return target == null ? null : DateExtension.format(target, "yyyy-MM-dd HH:mm:ss");
	}

	// 脚本中使用 date_format(now(),'yyyy-MM-dd')
	@Function
	@Comment("Date formatting")
	public String date_format(@Comment(name = "target", value = "Target date") Date target, @Comment(name = "pattern", value = "Format") String pattern) {
		return target == null ? null : DateExtension.format(target, pattern);
	}

	// 脚本中直接使用ifnull() 调用
	@Function
	@Comment("Check if value is empty")
	public Object ifnull(@Comment(name = "target", value = "Target value") Object target, @Comment(name = "trueValue", value = "Empty value") Object trueValue, @Comment(name = "falseValue", value = "Non-empty value") Object falseValue) {
		return target == null ? trueValue : falseValue;
	}

}
