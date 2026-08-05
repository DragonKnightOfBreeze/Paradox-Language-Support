package icu.windea.pls.inject

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.orNull
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.model.InjectionTargetInfo
import kotlin.reflect.full.findAnnotation

abstract class CodeInjectorBase : CodeInjector, UserDataHolderBase() {
    private val defaultId = javaClass.name

    override val id: String get() = defaultId

    final override fun inject() {
        val injectionTargetInfo = getInjectionTargetInfo() ?: return

        val pluginIdString = injectionTargetInfo.injectPluginId
        val pluginId = pluginIdString.orNull()?.let { PluginId.getId(it) }

        // skip if plugin of specified plugin id is not enabled
        if (pluginId != null && !isPluginEnabled(pluginId)) return

        val classPool = CodeInjectorContext.classPool ?: return
        val injectTargetName = injectionTargetInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorContext.targetClassKey, targetClass)

        applyCodeInjectorSupports()

        if (pluginId != null) {
            targetClass.toClass(getPluginClassLoader(pluginId), null)
        } else {
            targetClass.toClass()
        }
        targetClass.detach()

        // clean up
        putUserData(CodeInjectorContext.targetClassKey, null)
    }

    private fun isPluginEnabled(pluginId: PluginId): Boolean {
        return PluginManager.getInstance().findEnabledPlugin(pluginId) != null
    }

    private fun getPluginClassLoader(pluginId: PluginId): ClassLoader {
        // NOTE 3.0.0 [compatibility] `PluginManager.findEnabledPlugin(PluginId)` is internal (but ignored) since IDEA-262
        //  - Use `PluginDetailsService` instead (but by this way we cannot get the plugin class loader)
        val pluginDescriptor = PluginManager.getInstance().findEnabledPlugin(pluginId)
        return pluginDescriptor?.pluginClassLoader ?: PluginDescriptor::class.java.classLoader
    }

    private fun getInjectionTargetInfo(): InjectionTargetInfo? {
        val injectionTarget = this::class.findAnnotation<InjectionTarget>()
        if (injectionTarget == null) {
            thisLogger().error("Code injector $defaultId is not annotated with @InjectionTarget")
            return null
        }
        val injectTargetName = injectionTarget.value
        val injectPluginId = injectionTarget.pluginId
        return InjectionTargetInfo(this, injectTargetName, injectPluginId)
    }

    private fun applyCodeInjectorSupports() {
        CodeInjectorSupport.EP_NAME.extensionList.forEach { ep -> ep.apply(this) }
    }

    /** @see CodeInjectorContext.execute */
    @Suppress("unused")
    protected inline fun <T> execute(name: String, action: () -> T): T? {
        return CodeInjectorContext.execute(id, name, action)
    }

    /** @see CodeInjectorContext.reportError */
    @Suppress("unused")
    protected fun reportError(name: String, error: Throwable) {
        return CodeInjectorContext.reportError(id, name, error)
    }

    /** @see CodeInjectorContext.continueInvocation */
    @Suppress("unused")
    protected fun continueInvocation(): Nothing {
        CodeInjectorContext.continueInvocation()
    }
}
