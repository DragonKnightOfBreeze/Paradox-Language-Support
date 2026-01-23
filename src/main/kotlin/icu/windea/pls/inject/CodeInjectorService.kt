package icu.windea.pls.inject

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.application
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.staticProperty
import javassist.ClassPool

@Service
class CodeInjectorService : Disposable {
    private val logger = logger<CodeInjectorService>()

    /**
     * 用于在IDE启动时应用代码注入器。
     */
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }

    fun init() {
        if (!PlsFacade.isUnitTestMode()) {
            application.putUserData(
                CodeInjectorScope.applyInjectionMethodKey,
                CodeInjectorScope.javaClass.methods.first { it.name == "applyInjection" && it.parameterCount == 6 }
            )
        }

        CodeInjectorScope.classPool = CodeInjectorScope.getClassPool()

        val codeInjectors = CodeInjectorScope.codeInjectors
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            val codeInjectorId = codeInjector.id
            try {
                codeInjector.inject()
                logger.info("Applied code injector: $codeInjectorId")
            } catch (e: Exception) {
                // NOTE IDE更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                logger.warn("ERROR when applying code injector: $codeInjectorId")
                logger.warn(e.message, e)
            }
            codeInjectors.put(codeInjectorId, codeInjector)
        }

        // clean up class pool
        CodeInjectorScope.classPool = null
        // tricy but somehow necessary (~20M)
        staticProperty<ClassPool, ClassPool?>("defaultPool").set(null)
    }

    override fun dispose() {
        if (!PlsFacade.isUnitTestMode()) {
            application.putUserData(CodeInjectorScope.applyInjectionMethodKey, null)
        }

        // 避免内存泄露
        CodeInjectorScope.classPool = null
        CodeInjectorScope.codeInjectors.clear()
    }
}

