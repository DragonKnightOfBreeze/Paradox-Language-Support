package icu.windea.pls.inject

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.runOnce
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.model.InjectionTargetInfo
import kotlin.reflect.full.findAnnotation

abstract class CodeInjectorBase : CodeInjector, UserDataHolderBase() {
    override val id: String = javaClass.name

    final override fun inject() {
        val injectionTargetInfo = getInjectionTargetInfo() ?: return

        val pluginId = injectionTargetInfo.injectPluginId
        val enabledPlugin = pluginId.orNull()
            ?.let { PluginId.getId(it) }
            ?.let { PluginManager.getInstance().findEnabledPlugin(it) }
        // skip if plugin of specified plugin id is not enabled
        if (pluginId.isNotEmpty() && enabledPlugin == null) return

        val classPool = CodeInjectorUtil.classPool ?: return
        val injectTargetName = injectionTargetInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorUtil.targetClassKey, targetClass)

        applyCodeInjectorSupports()

        if (pluginId.isEmpty()) {
            targetClass.toClass()
        } else {
            val pluginClassLoader = runCatchingCancelable { enabledPlugin!!.pluginClassLoader }
                .getOrElse { PluginDescriptor::class.java.classLoader }
            targetClass.toClass(pluginClassLoader, null)
        }
        targetClass.detach()

        // clean up
        putUserData(CodeInjectorUtil.targetClassKey, null)
    }

    private fun getInjectionTargetInfo(): InjectionTargetInfo? {
        val injectionTarget = this::class.findAnnotation<InjectionTarget>()
        if (injectionTarget == null) {
            thisLogger().error("Code injector $id is not annotated with @InjectionTarget")
            return null
        }
        val injectTargetName = injectionTarget.value
        val injectPluginId = injectionTarget.pluginId
        return InjectionTargetInfo(this, injectTargetName, injectPluginId)
    }

    private fun applyCodeInjectorSupports() {
        CodeInjectorSupport.EP_NAME.extensionList.forEach { ep -> ep.apply(this) }
    }

    /**
     * 用于安全地执行注入的代码逻辑，并在发生异常时仅打印一次警告日志。
     */
    protected inline fun <T> runSafely(id: String = "", action: () -> T): T? {
        try {
            return action()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            val codeInjectorId = this.id
            val flagId = "$codeInjectorId.$id"
            val flag = CodeInjectorUtil.runSafelyFlags.get(flagId)
            runOnce(flag) {
                val logger = thisLogger()
                logger.warn("ERROR when executing injected code from code injector: $codeInjectorId (suppressed now)")
            }
            return null
        }
    }

    /**
     * 用于在（注入到目标方法之前的）注入方法中使用，让此方法不直接返回而继续执行目标方法中的代码。
     */
    protected fun continueInvocation(): Nothing = throw CodeInjectorUtil.continueInvocationException
}
