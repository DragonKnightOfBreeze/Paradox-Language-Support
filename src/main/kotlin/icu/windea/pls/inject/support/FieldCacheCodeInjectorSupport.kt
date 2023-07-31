package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import kotlin.reflect.full.*

/**
 * @see FieldCache
 */
class FieldCacheCodeInjectorSupport : CodeInjectorSupport() {
    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val fieldCache = codeInjector::class.findAnnotation<FieldCache>() ?: return
        val methodNames = fieldCache.methods
        val fieldPrefix = fieldCache.fieldPrefix
        val cleanupMethodName = fieldCache.cleanupMethod
        if(methodNames.isEmpty()) return
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
            val fieldName = fieldPrefix + methodName
            val returnTypeName = if(returnType is CtPrimitiveType) returnType.wrapperName else returnType.name
            val field = CtField.make("public volatile ${returnTypeName} ${fieldName} = null;", targetClass)
            targetClass.addField(field)
            val code1 = "{ if(${fieldName} != null) { return ${fieldName}; } }"
            method.insertBefore(code1)
            val code2 = "{ ${fieldName} = \$_; }"
            method.insertAfter(code2)
        }
        
        val s = finalMethodNames.joinToString("\n") { methodName -> "${fieldPrefix}${methodName} = null;" }
        val code = "{\n$s\n}"
        cleanupMethod.insertBefore(code)
    }
}