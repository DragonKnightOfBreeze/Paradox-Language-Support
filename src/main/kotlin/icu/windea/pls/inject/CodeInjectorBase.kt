package icu.windea.pls.inject

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.util.application
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import kotlin.reflect.full.findAnnotation

/**
 * @see InjectTarget
 * @see InjectMethod
 */
abstract class CodeInjectorBase : CodeInjector, UserDataHolderBase() {
    final override val id: String = javaClass.name

    final override fun inject() {
        val codeInjectorInfo = getCodeInjectorInfo() ?: return

        val classPool = application.getUserData(CodeInjectorService.classPoolKey) ?: return
        val injectTargetName = codeInjectorInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorService.targetClassKey, targetClass)

        applyCodeInjectorSupports()

        val pluginId = codeInjectorInfo.injectPluginId
        if (pluginId.isEmpty()) {
            targetClass.toClass()
        } else {
            val pluginClassLoader = runCatchingCancelable {
                PluginManager.getInstance().findEnabledPlugin(PluginId.findId(pluginId)!!)!!.pluginClassLoader
            }.getOrElse { PluginDescriptor::class.java.classLoader }
            targetClass.toClass(pluginClassLoader, null)
        }
        targetClass.detach()
    }

    private fun getCodeInjectorInfo(): CodeInjectorInfo? {
        val injectTarget = this::class.findAnnotation<InjectTarget>()
        if (injectTarget == null) {
            thisLogger().error("Code injector $id is not annotated with @InjectTarget")
            return null
        }
        val injectTargetName = injectTarget.value
        val injectPluginId = injectTarget.pluginId
        return CodeInjectorInfo(this, injectTargetName, injectPluginId)
    }

    private fun applyCodeInjectorSupports() {
        CodeInjectorSupport.EP_NAME.extensionList.forEach { ep ->
            ep.apply(this)
        }
    }

    private val CONTINUE_INVOCATION by lazy { application.getUserData(CodeInjectorService.continueInvocationExceptionKey)!! }

    /**
     * 用于在（注入到目标方法之前的）注入方法中使用，让此方法不直接返回而继续执行目标方法中的代码。
     */
    protected fun continueInvocation(): Nothing = throw CONTINUE_INVOCATION
}
