package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectReturnValue
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
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
            val injectMethodInfo = InjectMethodInfo(method, name, pointer, static, hasReceiver, returnValueParameterIndex)
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

            val targetArg = if (Modifier.isStatic(targetMethod.modifiers)) "null" else "$0"
            val returnValueArg = when (injectMethodInfo.pointer) {
                InjectMethod.Pointer.AFTER, InjectMethod.Pointer.AFTER_FINALLY -> "\$_"
                else -> "null"
            }

            // 需要兼容以下异常：
            // - java.lang.reflect.InvocationTargetException
            // - com.intellij.openapi.progress.ProcessCanceledException
            // - icu.windea.pls.inject.ContinueInvocationException

            val exprArgs = "\"${codeInjector.id}\", \"$methodId\", \$args, (\$w) $targetArg, (\$w) $returnValueArg"
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
            applyInjectedCode(injectMethodInfo, targetMethod, injectedCode)
        }
    }

    private fun findTargetMethod(injectMethodInfo: InjectMethodInfo, targetClass: CtClass, classPool: ClassPool): CtMethod? {
        val methodName = injectMethodInfo.name

        val normalParameterTypes = buildList {
            val parameterCount = injectMethodInfo.method.parameterCount
            for (i in 0 until parameterCount) {
                if (injectMethodInfo.hasReceiver && i == 0) continue
                if (injectMethodInfo.returnValueParameterIndex == i) continue
                add(injectMethodInfo.method.parameterTypes[i])
            }
        }
        val argSize = normalParameterTypes.size

        var ctMethods = targetClass.getDeclaredMethods(methodName).filter f@{ ctMethod ->
            val isStatic = Modifier.isStatic(ctMethod.modifiers)
            if ((injectMethodInfo.static && !isStatic) || (!injectMethodInfo.static && isStatic)) return@f false
            ctMethod.parameterTypes.size >= argSize
        }
        run {
            if (ctMethods.size <= 1) return@run
            ctMethods = ctMethods.filter { ctMethod ->
                for (i in 0 until argSize) {
                    val r = runCatchingCancelable {
                        val t1 = ctMethod.parameterTypes[i]
                        val t2 = normalParameterTypes[i]
                        val t3 = classPool.get(t2.name)
                        t1.subclassOf(t3)
                    }.getOrElse { true }
                    if (!r) return@filter false
                }
                true
            }
        }
        run {
            if (ctMethods.size <= 1) return@run
            ctMethods = ctMethods.filter { ctMethod ->
                ctMethod.parameterTypes.size >= argSize
            }
        }
        return ctMethods.firstOrNull()
    }

    private fun applyInjectedCode(injectMethodInfo: InjectMethodInfo, targetMethod: CtMethod, code: String) {
        when (injectMethodInfo.pointer) {
            InjectMethod.Pointer.BODY -> targetMethod.setBody(code)
            InjectMethod.Pointer.BEFORE -> targetMethod.insertBefore(code)
            InjectMethod.Pointer.AFTER -> targetMethod.insertAfter(code, false, targetMethod.declaringClass.isKotlin)
            InjectMethod.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(code, true, targetMethod.declaringClass.isKotlin)
        }
    }
}
