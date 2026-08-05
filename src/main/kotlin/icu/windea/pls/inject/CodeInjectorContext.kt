package icu.windea.pls.inject

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.util.application
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.staticProperty
import icu.windea.pls.core.util.createKey
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtClass
import kotlinx.coroutines.CancellationException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap

@PublishedApi
internal object CodeInjectorContext {
    private val logger = thisLogger()
    private val reported = ConcurrentHashMap.newKeySet<String>()

    // keys for `Application`
    val applyInjectionMethodKey = createKey<Method>("APPLY_INJECTION_METHOD_BY_WINDEA")

    // keys for `CodeInjector`
    val targetClassKey = createKey<CtClass>("TARGET_CLASS_BY_WINDEA")
    val injectMethodInfosKey = createKey<Map<String, InjectMethodInfo>>("INJECT_METHOD_INFOS_BY_WINDEA")

    @PublishedApi internal val codeInjectors: MutableMap<String, CodeInjector> = mutableMapOf()
    @PublishedApi internal val continueInvocationException: ContinueInvocationException = ContinueInvocationException("CONTINUE_INVOCATION_BY_WINDEA")
    @PublishedApi @Volatile internal var classPool: ClassPool? = null

    @PublishedApi
    internal fun init() {
        if (!ChronicleFacade.isUnitTestMode()) {
            application.putUserData(applyInjectionMethodKey, CodeInjectorContext.javaClass.methods.first { it.name == "applyInjection" })
        }

        // initialize class pool
        classPool = initClassPool()
        // apply code injectors
        applyCodeInjectors()

        // clean up class pool
        classPool = null // detach
        staticProperty<ClassPool, ClassPool?>("defaultPool").set(null) // tricky but somehow necessary (~20M)
    }

    @PublishedApi
    internal fun initClassPool(): ClassPool {
        val classPool = ClassPool.getDefault()
        val classPathList = System.getProperty("java.class.path")
        val separator = if (System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                classPool.appendClassPath(it)
            } catch (_: Exception) {
                // ignored
            }
        }
        classPool.appendClassPath(ClassClassPath(javaClass))
        return classPool
    }

    @PublishedApi
    internal fun applyCodeInjectors() {
        val codeInjectors = codeInjectors
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            val codeInjectorId = codeInjector.id
            try {
                codeInjector.inject()
                logger.info("Applied code injector: $codeInjectorId")
            } catch (e: Exception) {
                if (e is ProcessCanceledException || e is CancellationException) throw e
                logger.warn("ERROR while applying code injector: $codeInjectorId")
                logger.warn(e.message, e)
            }
            codeInjectors.put(codeInjectorId, codeInjector)
        }
    }

    @PublishedApi
    internal fun cleanUp() {
        if (!ChronicleFacade.isUnitTestMode()) {
            application.putUserData(applyInjectionMethodKey, null)
        }

        reported.clear()
        codeInjectors.clear()

        // clean up class pool
        classPool = null // detach
        staticProperty<ClassPool, ClassPool?>("defaultPool").set(null) // tricky but somehow necessary (~20M)
    }

    /**
     * 执行注入的代码逻辑，捕捉意外错误，并对于每个 [codeInjectorId] 的每个 [name] 仅报告一次错误。
     */
    @PublishedApi
    internal inline fun <T> execute(codeInjectorId: String, name: String, action: () -> T): T? {
        try {
            return action()
        } catch (e: Exception) {
            if (e is ProcessCanceledException || e is CancellationException) throw e
            reportError(codeInjectorId, name, e)
            return null
        }
    }

    /**
     * 报告错误（对于每个 [codeInjectorId] 的每个 [name] 仅报告一次）。
     */
    @PublishedApi
    internal fun reportError(codeInjectorId: String, name: String, error: Throwable) {
        val key = "$codeInjectorId@$name"
        if (!reported.add(key)) return
        logger.warn("ERROR while executing $name from code injector: $codeInjectorId (suppressed now)", error)
    }

    /**
     * 结束执行注入的代码逻辑，继续执行目标方法中的代码。用于在（注入到目标方法之前的）注入方法中使用。
     */
    @PublishedApi
    internal fun continueInvocation(): Nothing {
        throw continueInvocationException
    }

    @Suppress("unused")
    @JvmStatic
    @PublishedApi
    @Throws(InvocationTargetException::class)
    internal fun applyInjection(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?): Any? {
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

        var argIndex = 0
        for (i in 0 until actualArgsSize) {
            when {
                injectMethodInfo.hasReceiver && i == 0 -> {
                    finalArgs[i] = target
                }
                injectMethodInfo.returnValueParameterIndex == i -> {
                    finalArgs[i] = returnValue
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
        } catch (e: Exception) {
            if (ChronicleFacade.isUnitTestMode()) throw e
            if (e is InvocationTargetException) throw e.targetException ?: e // 3.0.1 fix: not `e.cause` here
            throw e
        }
    }
}
