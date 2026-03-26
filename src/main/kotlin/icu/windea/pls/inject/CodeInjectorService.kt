package icu.windea.pls.inject

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.util.application
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.staticProperty
import javassist.ClassPool

@Service
class CodeInjectorService : Disposable {
    private val logger = thisLogger()

    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }

    fun init() {
        if (!PlsFacade.isUnitTestMode()) {
            application.putUserData(CodeInjectorUtil.applyInjectionMethodKey, CodeInjectorUtil.javaClass.methods.first { it.name == "applyInjection" })
        }

        CodeInjectorUtil.classPool = CodeInjectorUtil.getClassPool()

        val codeInjectors = CodeInjectorUtil.codeInjectors
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            val codeInjectorId = codeInjector.id
            try {
                codeInjector.inject()
                logger.info("Applied code injector: $codeInjectorId")
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                // NOTE IDE 更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                logger.warn("ERROR when applying code injector: $codeInjectorId")
                logger.warn(e.message, e)
            }
            codeInjectors.put(codeInjectorId, codeInjector)
        }

        // clean up class pool
        CodeInjectorUtil.classPool = null
        // tricky but somehow necessary (~20M)
        staticProperty<ClassPool, ClassPool?>("defaultPool").set(null)
    }

    override fun dispose() {
        if (!PlsFacade.isUnitTestMode()) {
            application.putUserData(CodeInjectorUtil.applyInjectionMethodKey, null)
        }

        // 避免内存泄露
        CodeInjectorUtil.classPool = null
        CodeInjectorUtil.codeInjectors.clear()
        CodeInjectorUtil.runSafelyFlags.cleanUp()
    }
}

