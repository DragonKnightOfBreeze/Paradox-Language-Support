package icu.windea.pls.inject

import com.intellij.ide.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import icu.windea.pls.core.util.*
import icu.windea.pls.inject.support.*
import javassist.*
import java.lang.reflect.*

@Service
class CodeInjectorService {
    /**
     * 用于在IDE启动时应用代码注入器。
     */
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }

    companion object {
        //for Application
        @JvmField
        val classPoolKey = createKey<ClassPool>("CLASS_POOL_BY_WINDEA")
        //for Application
        @JvmField
        val codeInjectorsKey = createKey<Map<String, CodeInjector>>("CODE_INJECTORS_BY_WINDEA")
        //for Application
        @JvmField
        val invokeInjectMethodKey = createKey<Method>("INVOKE_INJECT_METHOD_BY_WINDEA")

        //for CodeInjector
        @JvmField
        val targetClassKey = createKey<CtClass>("TARGET_CLASS_BY_WINDEA")
        //for CodeInjector
        @JvmField
        val injectMethodInfosKey = createKey<Map<String, BaseCodeInjectorSupport.InjectMethodInfo>>("INJECT_METHOD_INFOS_BY_WINDEA")
    }

    fun init() {
        val application = ApplicationManager.getApplication()

        val classPool = getClassPool()
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        application.putUserData(classPoolKey, classPool)

        val codeInjectors = mutableMapOf<String, CodeInjector>()
        application.putUserData(codeInjectorsKey, codeInjectors)
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            try {
                codeInjector.inject()
            } catch (e: Exception) {
                //NOTE IDE更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                thisLogger().warn(e.message, e)
            }
            codeInjectors.put(codeInjector.id, codeInjector)
        }

        application.putUserData(classPoolKey, null)
    }

    private fun getClassPool(): ClassPool {
        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(this.javaClass))
        val classPathList = System.getProperty("java.class.path")
        val separator = if (System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                pool.appendClassPath(it)
            } catch (e: Exception) {
                //ignore
            }
        }
        return pool
    }
}
