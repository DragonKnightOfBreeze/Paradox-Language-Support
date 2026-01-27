package icu.windea.pls.inject

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
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
        // skip if plugin of specied plugin id is not enabled
        if (pluginId.isNotEmpty() && enabledPlugin == null) return

        val classPool = CodeInjectorScope.classPool ?: return
        val injectTargetName = injectionTargetInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorScope.targetClassKey, targetClass)

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
        putUserData(CodeInjectorScope.targetClassKey, null)
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
     * 用于在（注入到目标方法之前的）注入方法中使用，让此方法不直接返回而继续执行目标方法中的代码。
     */
    protected fun continueInvocation(): Nothing = throw CodeInjectorScope.continueInvocationException
}
