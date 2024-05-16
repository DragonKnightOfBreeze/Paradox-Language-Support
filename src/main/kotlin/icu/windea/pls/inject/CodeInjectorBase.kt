package icu.windea.pls.inject

import com.intellij.ide.plugins.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.inject.annotations.*
import kotlin.reflect.full.*

/**
 * @see InjectTarget
 * @see InjectMethod
 */
abstract class CodeInjectorBase : CodeInjector() {
    final override fun inject() {
        val codeInjectorInfo = getCodeInjectorInfo() ?: return
        
        val classPool = ApplicationManager.getApplication().getUserData(CodeInjectorService.classPoolKey) ?: return
        val injectTargetName = codeInjectorInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorService.targetClassKey, targetClass)
        
        applyCodeInjectorSupports()
        
        val pluginId = codeInjectorInfo.injectPluginId
        if(pluginId.isEmpty()) {
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
        if(injectTarget == null) {
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
    
    protected fun continueInvocation(): Nothing {
        throw CONTINUE_INVOCATION
    }
    
    companion object {
        private val CONTINUE_INVOCATION = ContinueInvocationException()
    }
}