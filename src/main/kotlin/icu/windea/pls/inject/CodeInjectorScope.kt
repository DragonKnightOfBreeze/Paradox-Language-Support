package icu.windea.pls.inject

import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.createKey
import icu.windea.pls.inject.SuperMethodInvoker
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtClass
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Parameter

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
    internal fun applyInjection(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?, superMethodInvoker: SuperMethodInvoker?): Any? {
        // 如果注入方法是一个扩展方法，则传递 `target` 到接收者（目标方法是一个静态方法时，`target` 的值为 `null`）
        // 如果注入方法的某个参数标记了 `@InjectReturnValue`，则传递 `returnValue` 到该参数（目标方法没有返回值时，`returnValue` 的值为 `null`）
        // 注入方法的余下参数按顺序传递到目标方法，其数量可以少于或等于目标方法的参数数量，但类型必须按顺序匹配
        // 不要在声明和调用注入方法时加载目标类型（例如，将接收者的类型直接指定为目标类型）

        val codeInjector = codeInjectors[codeInjectorId]
            ?: throw IllegalStateException("Cannot found code injector with id '$codeInjectorId'")
        val injectMethodInfo = codeInjector.getUserData(injectMethodInfosKey)?.get(methodId)
            ?: throw IllegalStateException("Cannot found inject method info with method id '$methodId'")
        val method = injectMethodInfo.method

        val parameters: Array<Parameter> = method.parameters
        val actualArgsSize = parameters.size
        val finalArgs = arrayOfNulls<Any?>(actualArgsSize)

        var computedSuperMethodInvoker: SuperMethodInvoker? = superMethodInvoker
        fun getOrCreateSuperMethodInvoker(): SuperMethodInvoker? {
            val existing = computedSuperMethodInvoker
            if (existing != null) return existing
            if (injectMethodInfo.superMethodParameterIndex < 0) return null
            val receiver = target ?: return null
            val bridgeName = "__pls_super_${injectMethodInfo.name}_$methodId"
            val bridgeMethod = receiver.javaClass.declaredMethods.firstOrNull { it.name == bridgeName && it.parameterCount == args.size }
                ?: return null
            bridgeMethod.isAccessible = true
            val created = SuperMethodInvoker { bridgeMethod.invoke(receiver, *args) }
            computedSuperMethodInvoker = created
            return created
        }

        var argIndex = 0
        for (i in 0 until actualArgsSize) {
            when {
                injectMethodInfo.hasReceiver && i == 0 -> {
                    finalArgs[i] = target
                }
                injectMethodInfo.returnValueParameterIndex == i -> {
                    finalArgs[i] = returnValue
                }
                injectMethodInfo.superMethodParameterIndex == i -> {
                    finalArgs[i] = getOrCreateSuperMethodInvoker()
                }
                else -> {
                    if (argIndex >= args.size) {
                        throw IllegalStateException("Cannot bind args for inject method '${method.name}': argIndex out of bounds ($argIndex >= ${args.size})")
                    }
                    finalArgs[i] = args[argIndex]
                    argIndex++
                }
            }
        }
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
