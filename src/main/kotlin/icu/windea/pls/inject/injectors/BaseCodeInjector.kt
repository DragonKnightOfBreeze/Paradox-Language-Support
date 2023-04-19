package icu.windea.pls.inject.injectors

import com.intellij.openapi.diagnostic.*
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
        codeInjectorInfo.injectMethods.forEach { (methodId, injectMethod) ->
            val targetMethod = findCtMethod(targetClass, injectMethod)
            if(targetMethod == null) {
                thisLogger().warn("Inject method ${injectMethod.name} cannot be applied to any methods of ${targetClass.name}")
                return@forEach
            }
            val injectMethodInfo = codeInjectorInfo.injectMethodInfos.get(methodId) ?: throw IllegalStateException()
            
            targetClass.addField(CtField.make("private static volatile UserDataHolder __codeInjectorService__ = null;", targetClass))
            targetClass.addField(CtField.make("private static volatile Method __invokeInjectMethodMethod__ = null;", targetClass))
            
            val targetArg = if(Modifier.isStatic(targetMethod.modifiers)) "null" else "$0"
            val returnTypeArg = if(injectMethodInfo.pointer == Inject.Pointer.BODY || injectMethodInfo.pointer == Inject.Pointer.BEFORE) "null" else "\$_"
            
            val args = "\"$id\", \"$methodId\", \$args, $targetArg, $returnTypeArg"
            javaClass.declaredMethods
            val code = """
            {
                try {
                    if(__codeInjectorService__ == null || __invokeInjectMethodMethod__ == null) { 
                        __codeInjectorService__ = (UserDataHolder) ApplicationManager.getApplication().getUserData(Key.findKeyByName("CODE_INJECTOR_SERVICE_BY_WINDEA"));
                        __invokeInjectMethodMethod__ = (Method) __codeInjectorService__.getUserData(Key.findKeyByName("INVOKE_METHOD_BY_WINDEA"));
                    }
                    return (${'$'}r) __invokeInjectMethodMethod__.invoke(__codeInjectorService__, new Object[] { $args });
                } catch(Throwable e) {
                    throw e;
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