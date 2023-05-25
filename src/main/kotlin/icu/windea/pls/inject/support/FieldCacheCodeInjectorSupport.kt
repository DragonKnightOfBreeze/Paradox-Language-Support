package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import kotlin.reflect.full.*

/**
 * @see FieldCacheMethods
 */
class FieldCacheCodeInjectorSupport : CodeInjectorSupport() {
    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return
        val fieldCacheMethods = codeInjector::class.findAnnotation<FieldCacheMethods>() ?: return
        val methodNames = fieldCacheMethods.methods
        val fieldPrefix = fieldCacheMethods.fieldPrefix
        val cleanUpMethodName = fieldCacheMethods.cleanUpMethod
        if(methodNames.isEmpty()) return
        val cleanUpMethod = targetClass.methods.find { it.name == cleanUpMethodName }
        if(cleanUpMethod == null) {
            thisLogger().warn("Clean up method ${cleanUpMethodName}() is not found in ${targetClass.name}")
            return
        }
        
        val finalMethodNames = mutableSetOf<String>()
        for(methodName in methodNames) {
            val method = targetClass.declaredMethods.find { it.name == methodName && it.parameterTypes.isEmpty() }
            if(method == null) {
                thisLogger().warn("Method ${methodName}() is not found in ${targetClass.name}")
                continue
            }
            val returnType = method.returnType
            if(returnType == null || returnType == CtClass.voidType) {
                thisLogger().warn("Method ${methodName}() returns nothing")
                continue
            }
            
            finalMethodNames.add(methodName)
            val fieldName = fieldPrefix + methodName
            val field = CtField.make("public volatile ${returnType.name} ${fieldName} = null;", targetClass)
            targetClass.addField(field)
            val code1 = "{ if(${fieldName} != null) { return ${fieldName}; } }"
            method.insertBefore(code1)
            val code2 = "{ ${fieldName} = \$_; }"
            method.insertAfter(code2)
        }
        
        val s = finalMethodNames.joinToString("\n") { methodName -> "${fieldPrefix}${methodName} = null;" }
        val code = "{\n$s\n}"
        cleanUpMethod.insertBefore(code)
    }
}