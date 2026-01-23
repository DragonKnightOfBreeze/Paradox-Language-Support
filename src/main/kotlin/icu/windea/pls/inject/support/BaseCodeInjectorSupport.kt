package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectReturnValue
import icu.windea.pls.inject.annotations.InjectSuperMethod
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.Modifier
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

/**
 * 为基于 [InjectMethod] 的代码注入器提供支持。
 *
 * 这类代码注入器可以替换或修改指定的方法。
 */
class BaseCodeInjectorSupport : CodeInjectorSupport {
    private val logger = thisLogger()

    private val applyInjectionMethodId get() = CodeInjectorScope.applyInjectionMethodKey.toString()
    private val continueInvocationExceptionId get() = CodeInjectorScope.continueInvocationException.message

    override fun apply(codeInjector: CodeInjector) {
        val classPool = CodeInjectorScope.classPool ?: return
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        classPool.importPackage("icu.windea.pls.inject")

        val targetClass = codeInjector.getUserData(CodeInjectorScope.targetClassKey) ?: return

        val functions = codeInjector::class.declaredFunctions
        if (functions.isEmpty()) return
        val injectMethodInfos = mutableMapOf<String, InjectMethodInfo>()
        var index = 0
        for (function in functions) {
            val info = function.findAnnotation<InjectMethod>() ?: continue
            val method = function.javaMethod ?: continue
            val methodId = index.toString()
            val name = info.value.ifEmpty { method.name }
            val pointer = info.pointer
            val static = info.static
            val hasReceiver = function.extensionReceiverParameter != null

            val returnValueParameterIndex = method.parameters.indexOfFirst { it.isAnnotationPresent(InjectReturnValue::class.java) }
            val superMethodParameterIndex = method.parameters.indexOfFirst { it.isAnnotationPresent(InjectSuperMethod::class.java) }
            val injectMethodInfo = InjectMethodInfo(method, name, pointer, static, hasReceiver, returnValueParameterIndex, superMethodParameterIndex)
            injectMethodInfos.put(methodId, injectMethodInfo)
            index++
        }
        if (injectMethodInfos.isEmpty()) return
        codeInjector.putUserData(CodeInjectorScope.injectMethodInfosKey, injectMethodInfos)

        if (!PlsFacade.isUnitTestMode()) {
            val code = "private static volatile Method __applyInjectionMethod__ = (Method) ApplicationManager.getApplication().getUserData(Key.findKeyByName(\"$applyInjectionMethodId\"));"
            targetClass.addField(CtField.make(code, targetClass))
        }

        for ((methodId, injectMethodInfo) in injectMethodInfos) {
            val targetMethod = findTargetMethod(injectMethodInfo, targetClass, classPool)
            if (targetMethod == null) {
                logger.warn("Inject method ${injectMethodInfo.method.name} cannot be applied to any method of ${targetClass.name}")
                continue
            }

            ensureSuperMethodBridgeMethod(targetClass, targetMethod, methodId, injectMethodInfo)

            val targetArg = if (Modifier.isStatic(targetMethod.modifiers)) "null" else "$0"
            val returnValueArg = when (injectMethodInfo.pointer) {
                InjectMethod.Pointer.AFTER, InjectMethod.Pointer.AFTER_FINALLY -> "\$_"
                else -> "null"
            }

            // 需要兼容以下异常：
            // - java.lang.reflect.InvocationTargetException
            // - com.intellij.openapi.progress.ProcessCanceledException
            // - icu.windea.pls.inject.ContinueInvocationException

            val exprArgs = "\"${codeInjector.id}\", \"$methodId\", \$args, (\$w) $targetArg, (\$w) $returnValueArg, null"
            val expr = when {
                PlsFacade.isUnitTestMode() -> "(\$r) CodeInjectorScope.applyInjection($exprArgs)"
                else -> "(\$r) __applyInjectionMethod__.invoke(null, new Object[] { $exprArgs })"
            }
            val throwExpr = when (injectMethodInfo.pointer) {
                InjectMethod.Pointer.BEFORE -> "if (!\"$continueInvocationExceptionId\".equals(__cause__.getMessage())) throw __cause__;"
                else -> "throw __cause__;"
            }
            val injectedCode = """
            {
                try {
                    return $expr;
                } catch(InvocationTargetException __e__) {
                    Throwable __cause__ = __e__.getCause();
                    if (__cause__ == null) throw __e__;
                    $throwExpr
                }
            }
            """.trimIndent()
            try {
                when (injectMethodInfo.pointer) {
                    InjectMethod.Pointer.BODY -> targetMethod.setBody(injectedCode)
                    InjectMethod.Pointer.BEFORE -> targetMethod.insertBefore(injectedCode)
                    InjectMethod.Pointer.AFTER -> targetMethod.insertAfter(injectedCode, false, targetMethod.declaringClass.isKotlin)
                    InjectMethod.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(injectedCode, true, targetMethod.declaringClass.isKotlin)
                }
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Cannot compile injected code for '${targetClass.name}#${targetMethod.name}' (inject method: '${injectMethodInfo.method.name}', pointer: '${injectMethodInfo.pointer}')\n" +
                        injectedCode,
                    e
                )
            }
        }
    }

    private fun findTargetMethod(injectMethodInfo: InjectMethodInfo, targetClass: CtClass, classPool: ClassPool): CtMethod? {
        val methodName = injectMethodInfo.name

        val normalParameterTypes = buildList {
            val parameterCount = injectMethodInfo.method.parameterCount
            for (i in 0 until parameterCount) {
                if (injectMethodInfo.hasReceiver && i == 0) continue
                if (injectMethodInfo.returnValueParameterIndex == i) continue
                if (injectMethodInfo.superMethodParameterIndex == i) continue
                add(injectMethodInfo.method.parameterTypes[i])
            }
        }
        val argSize = normalParameterTypes.size

        var ctMethods = targetClass.getDeclaredMethods(methodName).filter f@{ ctMethod ->
            val isStatic = Modifier.isStatic(ctMethod.modifiers)
            if ((injectMethodInfo.static && !isStatic) || (!injectMethodInfo.static && isStatic)) return@f false
            ctMethod.parameterTypes.size >= argSize
        }
        ctMethods = ctMethods.filter { ctMethod ->
            for (i in 0 until argSize) {
                val r = runCatchingCancelable {
                    val injectParameterType = normalParameterTypes[i]
                    val targetParameterType = ctMethod.parameterTypes[i]
                    isParameterCompatible(injectParameterType, targetParameterType, classPool)
                }.getOrElse { false }
                if (!r) return@filter false
            }
            true
        }
        return ctMethods.firstOrNull()
    }

    private fun ensureSuperMethodBridgeMethod(targetClass: CtClass, targetMethod: CtMethod, methodId: String, injectMethodInfo: InjectMethodInfo) {
        if (injectMethodInfo.superMethodParameterIndex < 0) return
        if (Modifier.isStatic(targetMethod.modifiers)) return

        val superClass = targetClass.superclass ?: return
        val superMethodExists = runCatchingCancelable {
            superClass.getMethod(targetMethod.name, targetMethod.signature)
        }.isSuccess
        if (!superMethodExists) return

        val bridgeName = "__pls_super_${targetMethod.name}_$methodId"
        if (targetClass.declaredMethods.any { it.name == bridgeName }) return

        val returnTypeText = targetMethod.returnType.name
        val parameterText = targetMethod.parameterTypes.mapIndexed { index, type -> "${type.name} p$index" }.joinToString(", ")
        val bodyText = if (targetMethod.returnType == CtClass.voidType) {
            "{ super.${targetMethod.name}(\$\$); }"
        } else {
            "{ return super.${targetMethod.name}(\$\$); }"
        }
        val code = "public $returnTypeText $bridgeName($parameterText) $bodyText"
        targetClass.addMethod(CtNewMethod.make(code, targetClass))
    }

    private fun isParameterCompatible(injectParameterType: Class<*>, targetParameterType: CtClass, classPool: ClassPool): Boolean {
        if (targetParameterType.isPrimitive) {
            val wrapperType = getPrimitiveWrapperType(targetParameterType) ?: return false
            return if (injectParameterType.isPrimitive) {
                getPrimitiveWrapperType(injectParameterType) == wrapperType
            } else {
                injectParameterType.isAssignableFrom(wrapperType)
            }
        }

        if (injectParameterType.isPrimitive) {
            val wrapperType = getPrimitiveWrapperType(injectParameterType) ?: return false
            val wrapperCtClass = classPool.get(wrapperType.name)
            return targetParameterType.subclassOf(wrapperCtClass)
        }

        val expectedCtClass = classPool.get(injectParameterType.name)
        return targetParameterType.subclassOf(expectedCtClass)
    }

    @Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun getPrimitiveWrapperType(type: Class<*>): Class<*>? {
        return when (type) {
            java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
            java.lang.Byte.TYPE -> java.lang.Byte::class.java
            java.lang.Character.TYPE -> java.lang.Character::class.java
            java.lang.Double.TYPE -> java.lang.Double::class.java
            java.lang.Float.TYPE -> java.lang.Float::class.java
            java.lang.Integer.TYPE -> java.lang.Integer::class.java
            java.lang.Long.TYPE -> java.lang.Long::class.java
            java.lang.Short.TYPE -> java.lang.Short::class.java
            java.lang.Void.TYPE -> java.lang.Void::class.java
            else -> null
        }
    }

    @Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun getPrimitiveWrapperType(type: CtClass): Class<*>? {
        return when (type) {
            CtClass.booleanType -> java.lang.Boolean::class.java
            CtClass.byteType -> java.lang.Byte::class.java
            CtClass.charType -> java.lang.Character::class.java
            CtClass.doubleType -> java.lang.Double::class.java
            CtClass.floatType -> java.lang.Float::class.java
            CtClass.intType -> java.lang.Integer::class.java
            CtClass.longType -> java.lang.Long::class.java
            CtClass.shortType -> java.lang.Short::class.java
            CtClass.voidType -> java.lang.Void::class.java
            else -> null
        }
    }
}
