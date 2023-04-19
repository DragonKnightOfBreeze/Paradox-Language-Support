package icu.windea.pls.inject

import com.intellij.openapi.diagnostic.*
import javassist.*
import java.lang.reflect.*

abstract class CodeInjectorBase : CodeInjector() {
    override fun inject() {
        val classPool = getUserData(CodeInjectorService.classPoolKey) ?: return
        val codeInjectorInfo = getUserData(CodeInjectorService.codeInjectorInfoKey) ?: return
        
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        
        val injectTargetName = codeInjectorInfo.injectTargetName
        val targetClass = classPool.get(injectTargetName)
        codeInjectorInfo.injectMethods.forEach { (methodId, injectMethod) ->
            val customizeMethod = findCtMethod(targetClass, injectMethod)
            if(customizeMethod == null) {
                thisLogger().warn("Inject method ${injectMethod.name} cannot be applied to any methods of ${targetClass.name}")
                return@forEach
            }
            val injectMethodInfo = codeInjectorInfo.injectMethodInfos.get(methodId) ?: throw IllegalStateException()
            
            val args = when(injectMethodInfo.pointer) {
                Inject.Pointer.BODY, Inject.Pointer.BEFORE -> "\"$id\", \"${methodId}\", \$args, $0, null"
                else -> "\"$id\", \"$methodId\", \$args, $0, \$_"
            }
            javaClass.declaredMethods
            val code = """
            {
                try {
                    UserDataHolder __codeInjectorService__ = (UserDataHolder) ApplicationManager.getApplication().getUserData(Key.findKeyByName("CODE_INJECTOR_SERVICE_BY_WINDEA"));
                    Method __invokeInjectMethodMethod__ = (Method) __codeInjectorService__.getUserData(Key.findKeyByName("INVOKE_METHOD_BY_WINDEA"));
                    return __invokeInjectMethodMethod__.invoke(__codeInjectorService__, new Object[] { $args });
                } catch(Throwable e) {
                    throw e;
                }
            }
            """.trimIndent()
            
            when(injectMethodInfo.pointer) {
                Inject.Pointer.BODY -> customizeMethod.setBody(code)
                Inject.Pointer.BEFORE -> customizeMethod.insertBefore(code)
                Inject.Pointer.AFTER -> customizeMethod.insertAfter(code, false, customizeMethod.declaringClass.isKotlin)
                Inject.Pointer.AFTER_FINALLY -> customizeMethod.insertAfter(code, true, customizeMethod.declaringClass.isKotlin)
            }
        }
        targetClass.toClass()
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