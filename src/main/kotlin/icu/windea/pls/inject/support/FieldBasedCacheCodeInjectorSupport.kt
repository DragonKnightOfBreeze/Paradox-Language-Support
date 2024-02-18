package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import kotlin.reflect.full.*

/**
 * 提供对字段缓存的代码注入器的支持。
 *
 * @see InjectFieldBasedCache
 */
class FieldBasedCacheCodeInjectorSupport : CodeInjectorSupport {
    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val injectFieldBasedCacheList = codeInjector::class.findAnnotations<InjectFieldBasedCache>()
        if(injectFieldBasedCacheList.isEmpty()) return
        
        val methodNamesGroup = mutableMapOf<String, MutableSet<String>>()
        injectFieldBasedCacheList.forEach { injectFieldBasedCache ->
            methodNamesGroup.getOrPut(injectFieldBasedCache.cleanup) { mutableSetOf() }.add(injectFieldBasedCache.value)
        }
        if(methodNamesGroup.isEmpty()) return
        
        val finalMethodNames = mutableSetOf<String>()
        for((cleanupMethodName, methodNames) in methodNamesGroup) {
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
            
            if(cleanupMethodName.isNotEmpty()) {
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
                continue
            }
        }
    }
}