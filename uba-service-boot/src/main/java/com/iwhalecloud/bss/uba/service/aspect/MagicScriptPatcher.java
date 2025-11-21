package com.iwhalecloud.bss.uba.service.aspect;

import javassist.*;

public class MagicScriptPatcher {

    private static boolean patched = false;

    /**
     * 使用 Javaassist 动态修改 MagicScript.execute 方法，在方法前后添加日志。
     * 此方法应在应用启动时调用一次。
     */
    public static void patch() {
        if (patched) {
            return;
        }
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass ctClass = pool.get("com.iwhalecloud.bss.magic.script.MagicScript");

            // 确保类只被修改一次
            if (ctClass.isFrozen()) {
                System.out.println("MagicScript class is already frozen. Cannot patch.");
                return;
            }

            CtMethod executeMethod = ctClass.getDeclaredMethod("execute", new CtClass[]{pool.get("com.iwhalecloud.bss.magic.script.MagicScriptContext")});

            // 在方法开始时插入代码
            executeMethod.insertBefore("{\n    System.out.println(\"Entering MagicScript.execute...\");\n}");

            // 在方法结束时插入代码（包括正常返回和异常返回）
            executeMethod.insertAfter("{\n    System.out.println(\"Exiting MagicScript.execute...\");\n}", true);

            // 加载修改后的类
            ctClass.toClass();

            System.out.println("MagicScript.execute method patched successfully.");
            patched = true;

        } catch (NotFoundException e) {
            System.err.println("MagicScript class or method not found. Is magic-script on the classpath?");
            e.printStackTrace();
        } catch (CannotCompileException e) {
            System.err.println("Failed to compile patched MagicScript class.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during MagicScript patching.");
            e.printStackTrace();
        }
    }
}
