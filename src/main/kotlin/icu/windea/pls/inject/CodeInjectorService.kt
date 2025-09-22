package icu.windea.pls.inject

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.application
import icu.windea.pls.core.util.createKey
import icu.windea.pls.inject.model.InjectMethodInfo
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtClass
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

private val logger = logger<CodeInjectorService>()

@Service
class CodeInjectorService {
    /**
     * 用于在IDE启动时应用代码注入器。
     */
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            service<CodeInjectorService>().init()
        }
    }

    fun init() {
        val application = application

        application.putUserData(invokeInjectMethodKey, javaClass.declaredMethods.first { it.name == "invokeInjectMethod" }.apply { trySetAccessible() })
        application.putUserData(continueInvocationExceptionKey, continueInvocationException)

        val classPool = getClassPool()
        classPool.importPackage("java.util")
        classPool.importPackage("java.lang.reflect")
        classPool.importPackage("com.intellij.openapi.application")
        classPool.importPackage("com.intellij.openapi.util")
        application.putUserData(classPoolKey, classPool)

        val codeInjectors = mutableMapOf<String, CodeInjector>()
        application.putUserData(codeInjectorsKey, codeInjectors)
        CodeInjector.EP_NAME.extensionList.forEach { codeInjector ->
            try {
                codeInjector.inject()
            } catch (e: Exception) {
                //NOTE IDE更新到新版本后，某些代码注入器可能已不再兼容，因而需要进行必要的验证和代码更改
                logger.warn("ERROR when applying injector: ${codeInjector.id}")
                logger.warn(e.message, e)
            }
            codeInjectors.put(codeInjector.id, codeInjector)
        }

        application.putUserData(classPoolKey, null)
    }

    private fun getClassPool(): ClassPool {
        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(this.javaClass))
        val classPathList = System.getProperty("java.class.path")
        val separator = if (System.getProperty("os.name")?.contains("linux") == true) ':' else ';'
        classPathList.split(separator).forEach {
            try {
                pool.appendClassPath(it)
            } catch (e: Exception) {
                logger.warn(e.message, e)
            }
        }
        return pool
    }

    companion object {
        //for Application
        @JvmField
        val invokeInjectMethodKey = createKey<Method>("INVOKE_INJECT_METHOD_BY_WINDEA")
        //for Application
        @JvmField
        val continueInvocationExceptionKey = createKey<Exception>("CONTINUE_INVOCATION_EXCEPTION_BY_WINDEA")

        //for Application
        @JvmField
        val classPoolKey = createKey<ClassPool>("CLASS_POOL_BY_WINDEA")
        //for Application
        @JvmField
        val codeInjectorsKey = createKey<Map<String, CodeInjector>>("CODE_INJECTORS_BY_WINDEA")

        //for CodeInjector
        @JvmField
        val targetClassKey = createKey<CtClass>("TARGET_CLASS_BY_WINDEA")
        //for CodeInjector
        @JvmField
        val injectMethodInfosKey = createKey<Map<String, InjectMethodInfo>>("INJECT_METHOD_INFOS_BY_WINDEA")

        //method invoked by injected codes

        @JvmStatic
        @Suppress("unused")
        private fun invokeInjectMethod(codeInjectorId: String, methodId: String, args: Array<out Any?>, target: Any?, returnValue: Any?): Any? {
            //如果注入方法是一个扩展方法，则传递target到接收者（目标方法是一个静态方法时，target的值为null）
            //如果注入方法拥有除了以上情况以外的额外参数，则传递returnValue到第1个额外参数（目标方法没有返回值时，returnValue的值为null）
            //不要在声明和调用注入方法时加载目标类型（例如，将接收者的类型直接指定为目标类型）
            val application = application
            val codeInjector = application.getUserData(codeInjectorsKey)?.get(codeInjectorId) ?: throw IllegalStateException()
            val injectMethodInfo = codeInjector.getUserData(injectMethodInfosKey)?.get(methodId) ?: throw IllegalStateException()
            val injectMethod = injectMethodInfo.method
            val actualArgsSize = injectMethod.parameterCount
            val finalArgs = when (actualArgsSize) {
                args.size -> args
                else -> {
                    @Suppress("RemoveExplicitTypeArguments")
                    buildList<Any?> {
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
            if (finalArgs.size != actualArgsSize) throw IllegalStateException()
            try {
                return injectMethod.invoke(codeInjector, *finalArgs)
            } catch (e: Exception) {
                if (e is InvocationTargetException) throw e.targetException
                throw e
            }
        }

        //exception thrown when should continue invocation

        @JvmStatic
        @Suppress("unused")
        private val continueInvocationException = ContinueInvocationException("CONTINUE_INVOCATION_BY_WINDEA")
    }
}
