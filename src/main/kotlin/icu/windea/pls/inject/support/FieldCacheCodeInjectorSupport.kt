package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.FieldCache
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import kotlin.reflect.full.findAnnotations

/**
 * 为基于 [FieldCache] 的代码注入器提供支持。
 *
 * 这类代码注入器可以通过注入的字段缓存访问器方法的返回值，并指定可选的清理方法。
 */
class FieldCacheCodeInjectorSupport : CodeInjectorSupport {
    private val logger = thisLogger()

    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorScope.targetClassKey) ?: return
        val infos = codeInjector::class.findAnnotations<FieldCache>()
        if (infos.isEmpty()) return

        val methodNamesGroup = mutableMapOf<String, MutableSet<String>>()
        infos.forEach { info ->
            methodNamesGroup.getOrPut(info.cleanUp) { mutableSetOf() }.add(info.value)
        }
        if (methodNamesGroup.isEmpty()) return

        for ((cleanupMethodName, methodNames) in methodNamesGroup) {
            for (methodName in methodNames) {
                var method = targetClass.declaredMethods.find { it.name == methodName && it.parameterTypes.isEmpty() }
                if (method == null) {
                    val superMethod = targetClass.methods.find { it.name == methodName && it.parameterTypes.isEmpty() }
                    if (superMethod != null) {
                        val m = CtMethod(superMethod, targetClass, null)
                        m.setBody("{ return super.${superMethod.name}($$); }")
                        targetClass.addMethod(m)
                        method = m
                    }
                }
                if (method == null) {
                    logger.warn("Method ${methodName}() not found in ${targetClass.name}")
                    continue
                }

                val returnType = method.returnType
                if (returnType == null || returnType == CtClass.voidType) {
                    logger.warn("Method ${methodName}() returns nothing")
                    continue
                }

                val fieldName = "__${methodName}__"
                if (targetClass.declaredFields.find { it.name == "__EMPTY_OBJECT__" } == null) {
                    val emptyObjectField = CtField.make("private static final Object __EMPTY_OBJECT__ = new Object();", targetClass)
                    targetClass.addField(emptyObjectField)
                }
                val field = CtField.make("private volatile Object ${fieldName} = __EMPTY_OBJECT__;", targetClass)
                targetClass.addField(field)
                val code1 = "{ if(${fieldName} != __EMPTY_OBJECT__) { return (\$r) ${fieldName}; } }"
                method.insertBefore(code1)
                val code2 = "{ ${fieldName} = (\$w)\$_; }"
                method.insertAfter(code2)
            }

            if (cleanupMethodName.isNotEmpty()) {
                var cleanupMethod = targetClass.declaredMethods.find { it.name == cleanupMethodName }
                if (cleanupMethod == null) {
                    val superCleanUpMethod = targetClass.methods.find { it.name == cleanupMethodName }
                    if (superCleanUpMethod != null) {
                        val m = CtMethod(superCleanUpMethod, targetClass, null)
                        m.setBody("{ return super.${superCleanUpMethod.name}($$); }")
                        targetClass.addMethod(m)
                        cleanupMethod = m
                    } else {
                        logger.warn("Clean up method ${cleanupMethodName}() is not found in ${targetClass.name}")
                        return
                    }
                }
                val s = methodNames.joinToString("\n") { methodName -> "__${methodName}__ = __EMPTY_OBJECT__;" }
                val code = "{\n$s\n}"
                cleanupMethod.insertBefore(code)
                continue
            }
        }
    }
}
