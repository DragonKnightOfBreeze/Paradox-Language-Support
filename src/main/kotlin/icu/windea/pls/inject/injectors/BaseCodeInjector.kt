package icu.windea.pls.inject.injectors

import com.intellij.ide.plugins.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.extensions.*
import icu.windea.pls.inject.*
import javassist.*
import javassist.Modifier
import java.lang.reflect.*

abstract class BaseCodeInjector : CodeInjector() {
    override fun inject() {
        val classPool = getUserData(CodeInjectorService.classPoolKey) ?: return
        val codeInjectorInfo = getUserData(CodeInjectorService.codeInjectorInfoKey) ?: return
        
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        
        val injectTargetName = codeInjectorInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        var addFields = true
        codeInjectorInfo.injectMethods.forEach { (methodId, injectMethod) ->
            val targetMethod = findCtMethod(targetClass, injectMethod)
            if(targetMethod == null) {
                thisLogger().warn("Inject method ${injectMethod.name} cannot be applied to any methods of ${targetClass.name}")
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
            val returnValueArg = if(injectMethodInfo.pointer == Inject.Pointer.AFTER || injectMethodInfo.pointer == Inject.Pointer.AFTER_FINALLY) "\$_" else  "null"
            
            val args = "new Object[] { \"$id\", \"$methodId\", \$args, $targetArg, $returnValueArg }"
            val code = "return (\$r) __invokeInjectMethodMethod__.invoke(__codeInjectorService__, $args);"
            when(injectMethodInfo.pointer) {
                Inject.Pointer.BODY -> targetMethod.setBody(code)
                Inject.Pointer.BEFORE -> targetMethod.insertBefore(code)
                Inject.Pointer.AFTER -> targetMethod.insertAfter(code, false, targetMethod.declaringClass.isKotlin)
                Inject.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(code, true, targetMethod.declaringClass.isKotlin)
            }
        }
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
    
    private fun findCtMethod(ctClass: CtClass, method: Method): CtMethod? {
        val methodName = method.name
        return ctClass.getDeclaredMethods(methodName).also { if(it.size == 1) return it[0] }
            .filter { it.parameterTypes.size <= method.parameterCount }.also { if(it.size == 1) return it[0] }
            .filter { it.parameterTypes.withIndex().all { (i, p) -> p.name == method.parameters[i].name } }.also { if(it.size == 1) return it[0] }
            .firstOrNull()
    }
}