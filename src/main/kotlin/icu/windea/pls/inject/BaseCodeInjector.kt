package icu.windea.pls.inject

import com.intellij.ide.plugins.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.inject.support.*
import javassist.*
import javassist.Modifier
import java.lang.reflect.*
import java.util.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

/**
 * @see InjectTarget
 * @see Inject
 */
abstract class BaseCodeInjector : CodeInjector() {
    final override fun inject() {
        val codeInjectorInfo = getCodeInjectorInfo() ?: return
        putUserData(CodeInjectorService.codeInjectorInfoKey, codeInjectorInfo)
        
        val classPool = getUserData(CodeInjectorService.classPoolKey) ?: return
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        
        val injectTargetName = codeInjectorInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        putUserData(CodeInjectorService.targetClassKey, targetClass)
        
        doInjectMethods(targetClass, codeInjectorInfo)
        applyCodeInjectorSupports()
        
        putUserData(CodeInjectorService.targetClassKey, null)
        val injectPluginId = codeInjectorInfo.injectPluginId
        if(injectPluginId.isEmpty()) {
            targetClass.toClass()
        } else {
            val pluginClassLoader = runCatching {
                PluginManager.getInstance().findEnabledPlugin(PluginId.findId(injectPluginId)!!)!!.pluginClassLoader
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
        val injectMethods = mutableMapOf<String, Method>()
        val injectMethodInfos = mutableMapOf<String, CodeInjectorInfo.MethodInfo>()
        val functions = this::class.declaredFunctions
        for(function in functions) {
            val inject = function.findAnnotation<Inject>() ?: continue
            val method = function.javaMethod ?: continue
            val uuid = UUID.randomUUID().toString()
            injectMethods.put(uuid, method)
            val hasReceiver = function.extensionReceiverParameter != null
            val injectMethodInfo = CodeInjectorInfo.MethodInfo(inject.pointer, hasReceiver)
            injectMethodInfos.put(uuid, injectMethodInfo)
        }
        return CodeInjectorInfo(this, injectTargetName, injectPluginId, injectMethods, injectMethodInfos)
    }
    
    private fun doInjectMethods(targetClass: CtClass, codeInjectorInfo: CodeInjectorInfo) {
        var addFields = true
        codeInjectorInfo.injectMethods.forEach { (methodId, injectMethod) ->
            val targetMethod = findCtMethod(targetClass, injectMethod)
            if(targetMethod == null) {
                thisLogger().warn("Inject method ${injectMethod.name} cannot be applied to any method of ${targetClass.name}")
                return@forEach
            }
            val injectMethodInfo = codeInjectorInfo.injectMethodInfos.get(methodId) ?: throw IllegalStateException()
            
            if(addFields) {
                val f1 = "private static volatile UserDataHolder __codeInjectorService__ = " +
                    "(UserDataHolder) ApplicationManager.getApplication().getUserData(Key.findKeyByName(\"CODE_INJECTOR_SERVICE_BY_WINDEA\"));"
                targetClass.addField(CtField.make(f1, targetClass))
                
                val f2 = "private static volatile Method __invokeInjectMethodMethod__ = " +
                    "(Method) __codeInjectorService__.getUserData(Key.findKeyByName(\"INVOKE_METHOD_BY_WINDEA\"));"
                targetClass.addField(CtField.make(f2, targetClass))
                addFields = false
            }
            
            val targetArg = if(Modifier.isStatic(targetMethod.modifiers)) "null" else "$0"
            val returnValueArg = if(injectMethodInfo.pointer == Inject.Pointer.AFTER || injectMethodInfo.pointer == Inject.Pointer.AFTER_FINALLY) "\$_" else "null"
            
            val args = "new Object[] { \"$id\", \"$methodId\", \$args, (\$w) $targetArg, (\$w) $returnValueArg }"
            val expr = "(\$r) __invokeInjectMethodMethod__.invoke(__codeInjectorService__, $args)"
            val code = if(injectMethodInfo.pointer != Inject.Pointer.BEFORE) "return $expr;" else """
                {
                    try {
                        return $expr;
                    } catch(InvocationTargetException __e__) {
                        if(!__e__.getCause().getCause().getClass().getName().equals("icu.windea.pls.inject.ContinueInvocationException")) throw __e__;
                    }
                }
                """.trimIndent()
            
            when(injectMethodInfo.pointer) {
                Inject.Pointer.BODY -> targetMethod.setBody(code)
                Inject.Pointer.BEFORE -> targetMethod.insertBefore(code)
                Inject.Pointer.AFTER -> targetMethod.insertAfter(code, false, targetMethod.declaringClass.isKotlin)
                Inject.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(code, true, targetMethod.declaringClass.isKotlin)
            }
        }
    }
    
    private fun applyCodeInjectorSupports() {
        CodeInjectorSupport.EP_NAME.extensionList.forEach { ep ->
            ep.apply(this)
        }
    }
    
    private fun findCtMethod(ctClass: CtClass, method: Method): CtMethod? {
        val methodName = method.name
        return ctClass.getDeclaredMethods(methodName).also { if(it.size == 1) return it[0] }
            .filter { it.parameterTypes.size <= method.parameterCount }.also { if(it.size == 1) return it[0] }
            .filter { it.parameterTypes.withIndex().all { (i, p) -> p.name == method.parameters[i].name } }.also { if(it.size == 1) return it[0] }
            .firstOrNull()
    }
    
    protected fun continueInvocation(): Nothing {
        throw CONTINUE_INVOCATION
    }
    
    companion object {
        private val CONTINUE_INVOCATION = ContinueInvocationException()
    }
}