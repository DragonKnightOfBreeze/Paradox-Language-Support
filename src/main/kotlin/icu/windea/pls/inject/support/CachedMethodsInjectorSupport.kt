package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import kotlin.reflect.full.*

/**
 * 用于支持基于字段缓存的方法。
 * @see InjectCachedMethods
 */
class CachedMethodsInjectorSupport : CodeInjectorSupport {
    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val injectCachedMethods = codeInjector::class.findAnnotation<InjectCachedMethods>() ?: return
        
        val methodNames = injectCachedMethods.methods
        if(methodNames.isEmpty()) return
        val finalMethodNames = mutableSetOf<String>()
        for(methodName in methodNames) {
            var method = targetClass.declaredMethods.find { it.name == methodName && it.parameterTypes.isEmpty() }
            if(method == null) {
                val superMethod = targetClass.methods.find { it.name == methodName && it.parameterTypes.isEmpty() }
                if(superMethod != null) {
                    val m = CtMethod(superMethod, targetClass, null)
                    m.setBody("{ return super.${superMethod.name}(\$\$); }")
                    targetClass.addMethod(m)
                    method = m
                } else {
                    thisLogger().warn("Method ${methodName}() is not found in ${targetClass.name}")
                    continue
                }
            }
            
            val returnType = method.returnType
            if(returnType == null || returnType == CtClass.voidType) {
                thisLogger().warn("Method ${methodName}() returns nothing")
                continue
            }
            
            finalMethodNames.add(methodName)
            val fieldName = "__${methodName}__"
            val returnTypeName = if(returnType is CtPrimitiveType) returnType.wrapperName else returnType.name
            if(targetClass.declaredFields.find { it.name == "__EMPTY_OBJECT__" } == null) {
                val emptyObjectField = CtField.make("private static final Object __EMPTY_OBJECT__ = new Object();", targetClass)
                targetClass.addField(emptyObjectField)
            }
            val field = CtField.make("private volatile Object ${fieldName} = __EMPTY_OBJECT__;", targetClass)
            targetClass.addField(field)
            val code1 = "{ if(${fieldName} != __EMPTY_OBJECT__) { return (${returnTypeName}) ${fieldName}; } }"
            method.insertBefore(code1)
            val code2 = "{ ${fieldName} = \$_; }"
            method.insertAfter(code2)
        }
        
        val cleanupMethodName = injectCachedMethods.cleanupMethod
        if(cleanupMethodName.isEmpty()) return //cleanup method is optional
        var cleanupMethod = targetClass.declaredMethods.find { it.name == cleanupMethodName }
        if(cleanupMethod == null) {
            val superCleanUpMethod = targetClass.methods.find { it.name == cleanupMethodName }
            if(superCleanUpMethod != null) {
                val m = CtMethod(superCleanUpMethod, targetClass, null)
                m.setBody("{ return super.${superCleanUpMethod.name}(\$\$); }")
                targetClass.addMethod(m)
                cleanupMethod = m
            } else {
                thisLogger().warn("Clean up method ${cleanupMethodName}() is not found in ${targetClass.name}")
                return
            }
        }
        val s = finalMethodNames.joinToString("\n") { methodName -> "__${methodName}__ = __EMPTY_OBJECT__;" }
        val code = "{\n$s\n}"
        cleanupMethod.insertBefore(code)
    }
}