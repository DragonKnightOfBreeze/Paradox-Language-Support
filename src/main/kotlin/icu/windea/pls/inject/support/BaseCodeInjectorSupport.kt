package icu.windea.pls.inject.support

import com.intellij.openapi.diagnostic.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import javassist.*
import javassist.Modifier
import java.lang.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

/**
 * 提供对基础的代码注入器的支持。
 *
 * @see InjectMethod
 */
class BaseCodeInjectorSupport : CodeInjectorSupport {
    data class InjectMethodInfo(
        val method: Method,
        val name: String,
        val pointer: InjectMethod.Pointer,
        val static: Boolean,
        val hasReceiver: Boolean,
        val hasReturnValue: Boolean
    )

    override fun apply(codeInjector: CodeInjector) {
        val targetClass = codeInjector.getUserData(CodeInjectorService.targetClassKey) ?: return

        val functions = codeInjector::class.declaredFunctions
        if (functions.isEmpty()) return
        val injectMethodInfos = mutableMapOf<String, InjectMethodInfo>()
        var index = 0
        for (function in functions) {
            val injectMethod = function.findAnnotation<InjectMethod>() ?: continue
            val method = function.javaMethod ?: continue
            val methodId = index.toString()
            val name = injectMethod.value.ifEmpty { method.name }
            val pointer = injectMethod.pointer
            val static = injectMethod.static
            val hasReceiver = function.extensionReceiverParameter != null
            val hasReturnValue = method.returnType != Void.TYPE && (pointer == InjectMethod.Pointer.AFTER || pointer == InjectMethod.Pointer.AFTER_FINALLY)
            val injectMethodInfo = InjectMethodInfo(method, name, pointer, static, hasReceiver, hasReturnValue)
            injectMethodInfos.put(methodId, injectMethodInfo)
            index++
        }
        if (injectMethodInfos.isEmpty()) return
        codeInjector.putUserData(CodeInjectorService.injectMethodInfosKey, injectMethodInfos)

        applyInjectMethods(codeInjector, targetClass, injectMethodInfos)
    }

    private fun applyInjectMethods(codeInjector: CodeInjector, targetClass: CtClass, injectMethodInfos: Map<String, InjectMethodInfo>) {
        val fieldCode = """private static volatile Method __invokeInjectMethod__ = (Method) ApplicationManager.getApplication().getUserData(Key.findKeyByName("INVOKE_INJECT_METHOD_BY_WINDEA"));"""
        targetClass.addField(CtField.make(fieldCode, targetClass))

        injectMethodInfos.forEach f@{ (methodId, injectMethodInfo) ->
            val injectMethod = injectMethodInfo.method
            val targetMethod = findCtMethod(targetClass, injectMethod, injectMethodInfo)
            if (targetMethod == null) {
                thisLogger().warn("Inject method ${injectMethod.name} cannot be applied to any method of ${targetClass.name}")
                return@f
            }

            val targetArg = if (Modifier.isStatic(targetMethod.modifiers)) "null" else "$0"
            val returnValueArg = if (injectMethodInfo.pointer == InjectMethod.Pointer.AFTER || injectMethodInfo.pointer == InjectMethod.Pointer.AFTER_FINALLY) "\$_" else "null"

            val args = "new Object[] { \"${codeInjector.id}\", \"$methodId\", \$args, (\$w) $targetArg, (\$w) $returnValueArg }"
            val expr = "(\$r) __invokeInjectMethod__.invoke(null, $args)"
            val code = when {
                injectMethodInfo.pointer != InjectMethod.Pointer.BEFORE -> {
                    """return $expr;"""
                }
                else -> {
                    """
                    {
                        try {
                            return $expr;
                        } catch(InvocationTargetException __e__) {
                            if(!"${CONTINUE_INVOCATION.message}".equals(__e__.getCause().getCause().getMessage())) throw __e__;
                        }
                    }
                    """.trimIndent()
                }
            }

            when (injectMethodInfo.pointer) {
                InjectMethod.Pointer.BODY -> targetMethod.setBody(code)
                InjectMethod.Pointer.BEFORE -> targetMethod.insertBefore(code)
                InjectMethod.Pointer.AFTER -> targetMethod.insertAfter(code, false, targetMethod.declaringClass.isKotlin)
                InjectMethod.Pointer.AFTER_FINALLY -> targetMethod.insertAfter(code, true, targetMethod.declaringClass.isKotlin)
            }
        }
    }

    private fun findCtMethod(ctClass: CtClass, method: Method, injectMethodInfo: InjectMethodInfo): CtMethod? {
        val application = application
        val methodName = injectMethodInfo.name
        var argSize = method.parameterCount
        if (injectMethodInfo.hasReceiver) argSize--
        if (injectMethodInfo.hasReturnValue) argSize--
        if (argSize < 0) return null //unexpected
        var argIndexOffset = 0
        if (injectMethodInfo.hasReceiver) argIndexOffset++
        var ctMethods = ctClass.getDeclaredMethods(methodName).filter f@{ ctMethod ->
            val isStatic = Modifier.isStatic(ctMethod.modifiers)
            if ((injectMethodInfo.static && !isStatic) || (!injectMethodInfo.static && isStatic)) return@f false
            ctMethod.parameterTypes.size >= argSize
        }
        run {
            if (ctMethods.size <= 1) return@run
            val classPool = application.getUserData(CodeInjectorService.classPoolKey) ?: return@run
            ctMethods = ctMethods.filter { ctMethod ->
                val size = ctMethod.parameterTypes.size
                for (i in 0 until size) {
                    val r = runCatchingCancelable {
                        val t1 = ctMethod.parameterTypes[i]
                        val t2 = method.parameterTypes[i + argIndexOffset]
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

    private val CONTINUE_INVOCATION by lazy { application.getUserData(CodeInjectorService.continueInvocationExceptionKey)!! }
}
