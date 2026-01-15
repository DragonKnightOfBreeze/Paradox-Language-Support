package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.CodeInjectorSupport
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.CtClass
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

    override fun apply(codeInjector: CodeInjector) {
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
            val hasReturnValue = method.returnType != Void.TYPE && (pointer == InjectMethod.Pointer.AFTER || pointer == InjectMethod.Pointer.AFTER_FINALLY)
            val injectMethodInfo = InjectMethodInfo(method, name, pointer, static, hasReceiver, hasReturnValue)
            injectMethodInfos.put(methodId, injectMethodInfo)
            index++
        }
        if (injectMethodInfos.isEmpty()) return
        codeInjector.putUserData(CodeInjectorScope.injectMethodInfosKey, injectMethodInfos)

        injectMethods(codeInjector, targetClass, injectMethodInfos)
    }

    private fun injectMethods(codeInjector: CodeInjector, targetClass: CtClass, injectMethodInfos: Map<String, InjectMethodInfo>) {
        injectMethodInfos.forEach f@{ (methodId, injectMethodInfo) ->
            val targetMethod = findTargetMethod(targetClass, injectMethodInfo)
            if (targetMethod == null) {
                logger.warn("Inject method ${injectMethodInfo.method.name} cannot be applied to any method of ${targetClass.name}")
                return@f
            }

            val code = getInjectedCode(codeInjector, methodId, injectMethodInfo, targetMethod)

            when (injectMethodInfo.pointer) {
                InjectMethod.Pointer.BODY -> targetMethod.setBody(code)
                InjectMethod.Pointer.BEFORE -> targetMethod.insertBefore(code)
                InjectMethod.Pointer.AFTER -> targetMethod.insertAfter(code, false, targetMethod.declaringClass.isKotlin)
                InjectMethod.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(code, true, targetMethod.declaringClass.isKotlin)
            }
        }
    }

    private fun findTargetMethod(targetClass: CtClass, injectMethodInfo: InjectMethodInfo): CtMethod? {
        val methodName = injectMethodInfo.name
        var argSize = injectMethodInfo.method.parameterCount
        if (injectMethodInfo.hasReceiver) argSize--
        if (injectMethodInfo.hasReturnValue) argSize--
        if (argSize < 0) return null // unexpected
        var argIndexOffset = 0
        if (injectMethodInfo.hasReceiver) argIndexOffset++
        var ctMethods = targetClass.getDeclaredMethods(methodName).filter f@{ ctMethod ->
            val isStatic = Modifier.isStatic(ctMethod.modifiers)
            if ((injectMethodInfo.static && !isStatic) || (!injectMethodInfo.static && isStatic)) return@f false
            ctMethod.parameterTypes.size >= argSize
        }
        run {
            if (ctMethods.size <= 1) return@run
            val classPool = CodeInjectorScope.classPool ?: return@run
            ctMethods = ctMethods.filter { ctMethod ->
                val size = ctMethod.parameterTypes.size
                for (i in 0 until size) {
                    val r = runCatchingCancelable {
                        val t1 = ctMethod.parameterTypes[i]
                        val t2 = injectMethodInfo.method.parameterTypes[i + argIndexOffset]
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
                ctMethod.parameterTypes.size == argSize
            }
        }
        return ctMethods.firstOrNull()
    }

    private fun getInjectedCode(codeInjector: CodeInjector, methodId: String, injectMethodInfo: InjectMethodInfo, targetMethod: CtMethod): String {
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
        val expr = "(\$r) CodeInjectorScope.applyInjection($exprArgs)"
        val throwExpr = when (injectMethodInfo.pointer) {
            InjectMethod.Pointer.BEFORE -> "if (!\"CONTINUE_INVOCATION_BY_WINDEA\".equals(__cause__.getMessage())) throw __cause__;"
            else -> "throw __cause__;"
        }
        val code = """
            {
                try {
                    return $expr;
                } catch(java.lang.reflect.InvocationTargetException __e__) {
                    Throwable __cause__ = __e__.getCause();
                    if (__cause__ == null) throw __e__;
                    $throwExpr
                }
            }
            """.trimIndent()
        return code
    }
}
