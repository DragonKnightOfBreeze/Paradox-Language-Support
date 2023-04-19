package icu.windea.pls.inject

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import javassist.*
import java.lang.reflect.*
import java.util.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

/**
 * 用于在IDE启动时应用代码注入器。
 *
 * @see CodeInjector
 */
class CodeInjectorService : UserDataHolderBase() {
    companion object {
        //for Application / CodeInjector
        @JvmField val codeInjectorServiceKey = Key.create<CodeInjectorService>("CODE_INJECTOR_SERVICE_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val classPoolKey = Key.create<ClassPool>("CLASS_POOL_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val invokeMethodKey = Key.create<Method>("INVOKE_METHOD_BY_WINDEA")
        //for CodeInjectorService
        @JvmField val codeInjectorsKey = Key.create<Map<String, CodeInjector>>("CODE_INJECTORS_BY_WINDEA")
        //for CodeInjector
        @JvmField val codeInjectorInfoKey = Key.create<CodeInjectorInfo>("CODE_INJECTOR_INFO_BY_WINDEA")
    }
    
    init {
        init()
    }
    
    fun init() {
        val application = ApplicationManager.getApplication()
        application.putUserDataIfAbsent(codeInjectorServiceKey, this)
        
        val classPool = getClassPool()
        putUserData(classPoolKey, classPool)
        
        val invokeMethod = javaClass.declaredMethods.find { it.name == "invokeInjectMethod" }!!
        putUserData(invokeMethodKey, invokeMethod)
        
        val codeInjectors = mutableMapOf<String, CodeInjector>()
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            handleCodeInjector(codeInjector)
            codeInjector.inject()
            codeInjectors.put(codeInjector.id, codeInjector)
        }
        putUserData(codeInjectorsKey, codeInjectors)
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
    
    private fun handleCodeInjector(codeInjector: CodeInjector) {
        codeInjector.putUserData(codeInjectorServiceKey, this)
        val injectTarget = codeInjector::class.findAnnotation<InjectTarget>()
        if(injectTarget == null) {
            thisLogger().error("Code injector ${codeInjector.id} is not annotated with @InjectTarget")
            return
        }
        val injectTargetName = injectTarget.value
        val injectMethods = mutableMapOf<String, Method>()
        val injectMethodInfos = mutableMapOf<String, InjectMethodInfo>()
        val functions = codeInjector::class.declaredFunctions
        for(function in functions) {
            val inject = function.findAnnotation<Inject>() ?: continue
            val method = function.javaMethod ?: continue
            val uuid = UUID.randomUUID().toString()
            injectMethods.put(uuid, method)
            val hasReceiver = function.extensionReceiverParameter != null
            val injectMethodInfo = InjectMethodInfo(inject.pointer, hasReceiver)
            injectMethodInfos.put(uuid, injectMethodInfo)
        }
        if(injectMethods.isEmpty()) {
            thisLogger().warn("Code inject ${codeInjector.id} has no inject methods")
            return
        }
        val codeInjectorInfo = CodeInjectorInfo(codeInjector, injectTargetName, injectMethods, injectMethodInfos)
        codeInjector.putUserData(codeInjectorInfoKey, codeInjectorInfo)
    }
    
    fun invokeInjectMethod(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?): Any? {
        //如果注入方法是一个扩展方法，则传递target到接收者参数（目标方法是一个静态方法时，target的值为null）
        //如果注入方法拥有除了以上情况以外的额外参数，则传递returnValue到第1个额外参数（目标方法没有返回值时，returnValue的值为null）
        val codeInjector = getUserData(codeInjectorsKey)?.get(codeInjectorId) ?: throw IllegalStateException()
        val codeInjectorInfo = codeInjector.getUserData(codeInjectorInfoKey) ?: throw IllegalStateException()
        val injectMethod = codeInjectorInfo.injectMethods[methodId] ?: throw IllegalStateException()
        val injectMethodInfo = codeInjectorInfo.injectMethodInfos[methodId] ?: throw IllegalStateException()
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
        if(finalArgs.size != injectMethod.parameterCount) throw IllegalStateException()
        return injectMethod.invoke(codeInjector, *finalArgs)
    }
}
