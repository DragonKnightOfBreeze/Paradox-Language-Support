package icu.windea.pls.inject.injectors

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectReturnValue
import icu.windea.pls.inject.annotations.InjectionTarget
import javassist.ClassClassPath
import javassist.CtClass
import javassist.CtNewConstructor
import javassist.CtNewMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("unused")
@RunWith(JUnit4::class)
class CodeInjectorReturnValueTest : BasePlatformTestCase() {
    private companion object {
        const val TARGET_RV_AT_START = "icu.windea.pls.inject.injectors.CodeInjectorReturnValueTest\$TargetReturnValueAtStart"
        const val TARGET_RV_AT_MIDDLE = "icu.windea.pls.inject.injectors.CodeInjectorReturnValueTest\$TargetReturnValueAtMiddle"
        const val TARGET_RV_AT_END = "icu.windea.pls.inject.injectors.CodeInjectorReturnValueTest\$TargetReturnValueAtEnd"
        const val TARGET_VOID_RV_NULL = "icu.windea.pls.inject.injectors.CodeInjectorReturnValueTest\$TargetVoidReturnValueNull"
    }

    override fun setUp() {
        super.setUp()
        CodeInjectorScope.classPool = CodeInjectorScope.getClassPool().also { it.appendClassPath(ClassClassPath(javaClass)) }
        CodeInjectorScope.codeInjectors.clear()
    }

    override fun tearDown() {
        try {
            CodeInjectorScope.classPool = null
            CodeInjectorScope.codeInjectors.clear()
        } finally {
            super.tearDown()
        }
    }

    private fun makeTargetClass(className: String, methods: List<String>): CtClass {
        val pool = CodeInjectorScope.classPool ?: error("ClassPool is not initialized")
        val ctClass = pool.makeClass(className)
        ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass))
        methods.forEach { ctClass.addMethod(CtNewMethod.make(it, ctClass)) }
        ctClass.stopPruning(true)
        return ctClass
    }

    private fun registerInjector(codeInjector: CodeInjector) {
        CodeInjectorScope.codeInjectors[codeInjector.id] = codeInjector
    }

    @Test
    fun test_returnValue_atStart() {
        makeTargetClass(
            TARGET_RV_AT_START,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_RV_AT_START, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.AFTER)
            fun after(@InjectReturnValue returnValue: Int, a: Int, b: Int): Int {
                return returnValue * 100 + a * 10 + b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_RV_AT_START, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(312, method.invoke(instance, 1, 2))
    }

    @Test
    fun test_returnValue_atMiddle() {
        makeTargetClass(
            TARGET_RV_AT_MIDDLE,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_RV_AT_MIDDLE, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.AFTER)
            fun after(a: Int, @InjectReturnValue returnValue: Int, b: Int): Int {
                return returnValue * 100 + a * 10 + b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_RV_AT_MIDDLE, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(312, method.invoke(instance, 1, 2))
    }

    @Test
    fun test_returnValue_atEnd() {
        makeTargetClass(
            TARGET_RV_AT_END,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_RV_AT_END, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.AFTER)
            fun after(a: Int, b: Int, @InjectReturnValue returnValue: Int): Int {
                return returnValue * 100 + a * 10 + b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_RV_AT_END, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(312, method.invoke(instance, 1, 2))
    }

    @Test
    fun test_returnValue_voidTarget_shouldBeNull() {
        makeTargetClass(
            TARGET_VOID_RV_NULL,
            listOf(
                "public void run(int a) { }"
            )
        )

        var capturedReturnValue: Any? = "__unset__"

        @InjectionTarget(TARGET_VOID_RV_NULL, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "run", pointer = InjectMethod.Pointer.AFTER)
            fun after(a: Int, @InjectReturnValue returnValue: Any?): Int {
                capturedReturnValue = returnValue
                return a
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_VOID_RV_NULL, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("run", Int::class.javaPrimitiveType)
        method.invoke(instance, 1)

        assertNull(capturedReturnValue)
    }
}
