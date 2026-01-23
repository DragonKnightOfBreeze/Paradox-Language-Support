package icu.windea.pls.inject

import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.createKey
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtClass
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Suppress("unused")
object CodeInjectorScope {
    // keys for `Application`
    @JvmField val applyInjectionMethodKey = createKey<Method>("APPLY_INJECTION_METHOD_BY_WINDEA")

    // keys for `CodeInjector`
    @JvmField val targetClassKey = createKey<CtClass>("TARGET_CLASS_BY_WINDEA")
    @JvmField val injectMethodInfosKey = createKey<Map<String, InjectMethodInfo>>("INJECT_METHOD_INFOS_BY_WINDEA")

    @PublishedApi @JvmField @Volatile internal var classPool: ClassPool? = null
    @PublishedApi @JvmField internal val codeInjectors: MutableMap<String, CodeInjector> = mutableMapOf()
    @PublishedApi @JvmField internal val continueInvocationException: ContinueInvocationException = ContinueInvocationException("CONTINUE_INVOCATION_BY_WINDEA")

    @PublishedApi
    @JvmStatic
    internal fun getClassPool(): ClassPool {
        val classPool = ClassPool.getDefault()
        classPool.appendClassPath(ClassClassPath(javaClass))
        val classPathList = System.getProperty("java.class.path")
        val separator = if (System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                classPool.appendClassPath(it)
            } catch (_: Exception) {
                // ignored
            }
        }
        return classPool
    }

    @Throws(InvocationTargetException::class)
    @PublishedApi
    @JvmStatic
    internal fun applyInjection(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?): Any? {
        // 如果注入方法是一个扩展方法，则传递 `target` 到接收者（目标方法是一个静态方法时，`target` 的值为 `null`）
        // 如果注入方法拥有除了以上情况以外的额外参数，则传递 `returnValue` 到第1个额外参数（目标方法没有返回值时，`returnValue` 的值为 `null`）
        // 不要在声明和调用注入方法时加载目标类型（例如，将接收者的类型直接指定为目标类型）

        val codeInjector = codeInjectors[codeInjectorId]
            ?: throw IllegalStateException("Cannot found code injector with id '$codeInjectorId'")
        val injectMethodInfo = codeInjector.getUserData(injectMethodInfosKey)?.get(methodId)
            ?: throw IllegalStateException("Cannot found inject method info with method id '$methodId'")
        val method = injectMethodInfo.method
        val actualArgsSize = method.parameterCount
        val finalArgs = when (actualArgsSize) {
            args.size -> args
            else -> {
                buildList {
                    if (injectMethodInfo.hasReceiver) {
                        add(target)
                    }
                    addAll(args)
                    if (size < actualArgsSize) {
                        add(returnValue)
                    }
                }.toTypedArray()
            }
        }
        val finalArgsSize = finalArgs.size
        if (finalArgsSize != actualArgsSize) throw IllegalStateException("FInal args size != actual args size ($finalArgsSize != ${actualArgsSize})")
        try {
            return method.invoke(codeInjector, *finalArgs)
        } catch (e: InvocationTargetException) {
            if (!PlsFacade.isUnitTestMode()) {
                throw e.targetException ?: e
            }
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
}
