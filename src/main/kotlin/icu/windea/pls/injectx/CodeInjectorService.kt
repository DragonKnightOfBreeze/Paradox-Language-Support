package icu.windea.pls.injectx

import com.intellij.ide.*
import com.intellij.ide.plugins.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.injectx.annotations.*
import net.bytebuddy.*
import net.bytebuddy.agent.*
import net.bytebuddy.implementation.*
import net.bytebuddy.matcher.ElementMatchers
import kotlin.reflect.full.*

@Service
class CodeInjectorService {
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }
    
    fun init() {
        ByteBuddyAgent.install()
        
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            try {
                applyCodeInjector(codeInjector)
            } catch(e: Exception) {
                //NOTE IDE更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                thisLogger().warn("Cannot apply code injector ${codeInjector::class.qualifiedName}", e)
            }
        }
    }
    
    private fun applyCodeInjector(codeInjector: CodeInjector) {
        val injectAnnotation = codeInjector::class.findAnnotation<Inject>()
        if(injectAnnotation == null) throw IllegalStateException("Not annotated with @Inject")
        
        val targetClass = injectAnnotation.value
        val pluginId = injectAnnotation.pluginId
        val targetClassLoader = when {
            pluginId.isEmpty() -> null
            else -> runCatchingCancelable {
                PluginManager.getInstance().findEnabledPlugin(PluginId.findId(pluginId)!!)!!.pluginClassLoader
            }.getOrElse { PluginDescriptor::class.java.classLoader }
        }
        
        val methodsWithAnnotations = codeInjector::class.declaredFunctions.mapNotNull f@{ method ->
            val injectMethodAnnotation = method.findAnnotation<InjectMethod>() ?: return@f null
            method to injectMethodAnnotation
        }
        if(methodsWithAnnotations.isEmpty()) return
        
        var builder = ByteBuddy().redefine(targetClass.java)
        methodsWithAnnotations.forEach { (method, injectMethodAnnotation) ->
            val targetMethodName = injectMethodAnnotation.value.ifEmpty { method.name }
            builder = builder.method(ElementMatchers.named(targetMethodName)).intercept(MethodDelegation.to(codeInjector))
        }
        builder.make().load(targetClassLoader)
    }
}
