package icu.windea.pls.inject.injectors

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import javassist.ClassClassPath
import javassist.CtClass
import javassist.CtNewConstructor
import javassist.CtNewMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
@RunWith(JUnit4::class)
class CodeInjectorParameterBindingTest : BasePlatformTestCase() {
    private companion object {
        const val TARGET_LESS_ARGS = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetLessArgs"
        const val TARGET_EQUAL_ARGS = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetEqualArgs"
        const val TARGET_MORE_ARGS = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetMoreArgs"
        const val TARGET_MISMATCH_TYPES = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetMismatchTypes"
        const val TARGET_SUSPEND = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetSuspend"
        const val TARGET_BOXED_TARGET_PRIMITIVE_INJECT = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetBoxedTargetPrimitiveInject"
        const val TARGET_PRIMITIVE_TARGET_BOXED_INJECT = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetPrimitiveTargetBoxedInject"
        const val TARGET_ARRAY_INT = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetArrayInt"
        const val TARGET_ARRAY_OBJECT = "icu.windea.pls.inject.injectors.CodeInjectorParameterBindingTest\$TargetArrayObject"
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

    private fun newNoopContinuation(): Continuation<Any?> {
        return object : Continuation<Any?> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Any?>) {
            }
        }
    }

    @Test
    fun test_argsCount_lessThanTarget() {
        makeTargetClass(
            TARGET_LESS_ARGS,
            listOf(
                "public int sum3(int a, int b, int c) { return a + b + c; }"
            )
        )

        @InjectionTarget(TARGET_LESS_ARGS, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum3", pointer = InjectMethod.Pointer.BODY)
            fun sum3(a: Int): Int = a
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_LESS_ARGS, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod(
            "sum3",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        assertEquals(1, method.invoke(instance, 1, 2, 3))
    }

    @Test
    fun test_argsCount_equalToTarget() {
        makeTargetClass(
            TARGET_EQUAL_ARGS,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_EQUAL_ARGS, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: Int, b: Int): Int = a * b
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_EQUAL_ARGS, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(6, method.invoke(instance, 2, 3))
    }

    @Test
    fun test_argsCount_moreThanTarget_notApplied() {
        makeTargetClass(
            TARGET_MORE_ARGS,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_MORE_ARGS, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: Int, b: Int, c: Int): Int = a * b * c
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_MORE_ARGS, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(5, method.invoke(instance, 2, 3))
    }

    @Test
    fun test_argsType_mismatch_notApplied() {
        makeTargetClass(
            TARGET_MISMATCH_TYPES,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_MISMATCH_TYPES, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: String): Int = a.length
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_MISMATCH_TYPES, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(5, method.invoke(instance, 2, 3))
    }

    @Suppress("RedundantSuspendModifier")
    @Test
    fun test_suspendTarget_and_suspendInjectMethod() {
        makeTargetClass(
            TARGET_SUSPEND,
            listOf(
                "public java.lang.Object sum(int a, int b, kotlin.coroutines.Continuation c) { return java.lang.Integer.valueOf(a + b); }"
            )
        )

        @InjectionTarget(TARGET_SUSPEND, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            suspend fun sum(a: Int, b: Int): Int {
                return a - b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_SUSPEND, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod(
            "sum",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Continuation::class.java
        )
        val continuation = newNoopContinuation()
        val result = method.invoke(instance, 2, 3, continuation)
        assertEquals(-1, (result as Number).toInt())
    }

    @Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Test
    fun test_primitiveTarget_boxedInjectMethod_shouldApply() {
        makeTargetClass(
            TARGET_PRIMITIVE_TARGET_BOXED_INJECT,
            listOf(
                "public int id(int a) { return a; }"
            )
        )

        @InjectionTarget(TARGET_PRIMITIVE_TARGET_BOXED_INJECT, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "id", pointer = InjectMethod.Pointer.BODY)
            fun id(a: java.lang.Integer): Int {
                return a.toInt() + 1
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_PRIMITIVE_TARGET_BOXED_INJECT, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("id", Int::class.javaPrimitiveType)
        assertEquals(2, method.invoke(instance, 1))
    }

    @Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @Test
    fun test_boxedTarget_primitiveInjectMethod_shouldApply() {
        makeTargetClass(
            TARGET_BOXED_TARGET_PRIMITIVE_INJECT,
            listOf(
                "public int id(java.lang.Integer a) { return a.intValue(); }"
            )
        )

        @InjectionTarget(TARGET_BOXED_TARGET_PRIMITIVE_INJECT, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "id", pointer = InjectMethod.Pointer.BODY)
            fun id(a: Int): Int {
                return a + 1
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_BOXED_TARGET_PRIMITIVE_INJECT, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("id", java.lang.Integer::class.java)
        assertEquals(2, method.invoke(instance, 1))
    }

    @Test
    fun test_arrayParameter_match_intArray() {
        makeTargetClass(
            TARGET_ARRAY_INT,
            listOf(
                "public int len(int[] a) { return a == null ? -1 : a.length; }"
            )
        )

        @InjectionTarget(TARGET_ARRAY_INT, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "len", pointer = InjectMethod.Pointer.BODY)
            fun len(a: IntArray): Int {
                return if (a.isEmpty()) 0 else a.size + 10
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_ARRAY_INT, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("len", IntArray::class.java)
        assertEquals(13, method.invoke(instance, intArrayOf(1, 2, 3)))
    }

    @Suppress("BoxArray")
    @Test
    fun test_arrayParameter_mismatch_shouldNotApply() {
        makeTargetClass(
            TARGET_ARRAY_OBJECT,
            listOf(
                "public int len(java.lang.Object[] a) { return a == null ? -1 : a.length; }"
            )
        )

        @InjectionTarget(TARGET_ARRAY_OBJECT, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "len", pointer = InjectMethod.Pointer.BODY)
            fun len(a: IntArray): Int {
                return a.size + 10
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_ARRAY_OBJECT, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("len", Array<Any>::class.java)
        assertEquals(3, method.invoke(instance, arrayOf(1, 2, 3)))
    }
}
