package com.iwhalecloud.bss.uba.service.scripts;

import com.iwhalecloud.bss.magic.magicapi.core.annotation.MagicModule;
import com.iwhalecloud.bss.magic.magicapi.modules.db.SQLModule;
import com.iwhalecloud.bss.magic.magicapi.modules.http.HttpModule;
import com.iwhalecloud.bss.magic.magicapi.modules.servlet.RequestModule;
import com.iwhalecloud.bss.magic.magicapi.modules.servlet.ResponseModule;
import com.iwhalecloud.bss.magic.script.annotation.Comment;

/**
 * 自定义模块
 * 脚本中使用
 * import custom;    //导入模块
 * custom.println('Custom Module!');
 *
 * https://ssssssss.org/magic-api/pages/senior/module/
 *
 * @see MagicModule
 * @see SQLModule
 * @see HttpModule
 * @see RequestModule
 * @see ResponseModule
 */
@MagicModule("custom")
public class CustomModule {

	@Comment("方法名的注释(用于提示)")
	public void println(@Comment(name = "value", value = "参数名的提示(用于提示)")String value) {
		System.out.println(value);
	}
}
