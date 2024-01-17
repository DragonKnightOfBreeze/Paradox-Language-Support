package icu.windea.pls.inject

import com.intellij.ide.*
import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.util.*
import javassist.*
import java.lang.reflect.*

@Service
class CodeInjectorService : UserDataHolderBase() {
    /**
     * 用于在IDE启动时应用代码注入器。
     */
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }
    
    companion object {
        //for Application / CodeInjector
        @JvmField val codeInjectorServiceKey = createKey<CodeInjectorService>("CODE_INJECTOR_SERVICE_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val classPoolKey = createKey<ClassPool>("CLASS_POOL_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val codeInjectorsKey = createKey<Map<String, CodeInjector>>("CODE_INJECTORS_BY_WINDEA")
        
        //for CodeInjector
        @JvmField val targetClassKey = createKey<CtClass>("TARGET_CLASS_BY_WINDEA")
        //for CodeInjector
        @JvmField val codeInjectorInfoKey = createKey<CodeInjectorInfo>("CODE_INJECTOR_INFO_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val invokeMethodKey = createKey<Method>("INVOKE_METHOD_BY_WINDEA")
    }
    
    fun init() {
        val application = ApplicationManager.getApplication()
        if(application.getUserData(codeInjectorServiceKey) == null) {
            application.putUserData(codeInjectorServiceKey, this)
        }
        
        val classPool = getClassPool()
        putUserData(classPoolKey, classPool)
        
        val invokeMethod = javaClass.declaredMethods.find { it.name == "invokeInjectMethod" }!!
        invokeMethod.trySetAccessible()
        putUserData(invokeMethodKey, invokeMethod)
        
        val codeInjectors = mutableMapOf<String, CodeInjector>()
        putUserData(codeInjectorsKey, codeInjectors)
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            codeInjector.putUserData(codeInjectorServiceKey, this)
            try {
                codeInjector.inject()
            } catch(e: Exception) {
                //NOTE IDE更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                thisLogger().warn(e.message, e)
            }
            codeInjectors.put(codeInjector.id, codeInjector)
        }
        
        putUserData(classPoolKey, null)
    }
    
    private fun getClassPool(): ClassPool {
        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(this.javaClass))
        val classPathList = System.getProperty("java.class.path")
        val separator = if(System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                pool.appendClassPath(it)
            } catch(e: Exception) {
                //ignore
            }
        }
        return pool
    }
    
    //methods invoked by injected codes 
    
    @Suppress("unused")
    private fun invokeInjectMethod(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?): Any? {
        //如果注入方法是一个扩展方法，则传递target到接收者（目标方法是一个静态方法时，target的值为null）
        //如果注入方法拥有除了以上情况以外的额外参数，则传递returnValue到第1个额外参数（目标方法没有返回值时，returnValue的值为null）
        //不要在声明和调用注入方法时加载目标类型（例如，将接收者的类型直接指定为目标类型）
        val codeInjector = getUserData(codeInjectorsKey)?.get(codeInjectorId) ?: throw IllegalStateException()
        val codeInjectorInfo = codeInjector.getUserData(codeInjectorInfoKey) ?: throw IllegalStateException()
        val injectMethodInfo = codeInjectorInfo.injectMethodInfos[methodId] ?: throw IllegalStateException()
        val injectMethod = injectMethodInfo.method
        val actualArgsSize = injectMethod.parameterCount
        val finalArgs = when(actualArgsSize) {
            args.size -> args
            else -> {
                @Suppress("RemoveExplicitTypeArguments")
                buildList<Any?> {
                    if(injectMethodInfo.hasReceiver) {
                        add(target)
                    }
                    addAll(args)
                    if(size < actualArgsSize) {
                        add(returnValue)
                    }
                }.toTypedArray()
            }
        }
        if(finalArgs.size != actualArgsSize) throw IllegalStateException()
        return injectMethod.invoke(codeInjector, *finalArgs)
    }
}
