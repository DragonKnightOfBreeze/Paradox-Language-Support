package icu.windea.pls.inject

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.OptimizedField
import javassist.ClassClassPath
import javassist.CtClass
import javassist.CtNewConstructor
import javassist.CtNewMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Suppress("unused")
@RunWith(JUnit4::class)
class CodeInjectorsTest : BasePlatformTestCase() {
    private companion object {
        const val TARGET_BEFORE = "icu.windea.pls.inject.CodeInjectorsTest\$TargetBefore"
        const val TARGET_BODY = "icu.windea.pls.inject.CodeInjectorsTest\$TargetBody"
        const val TARGET_AFTER = "icu.windea.pls.inject.CodeInjectorsTest\$TargetAfter"
        const val TARGET_AFTER_FINALLY = "icu.windea.pls.inject.CodeInjectorsTest\$TargetAfterFinally"
        const val TARGET_STATIC = "icu.windea.pls.inject.CodeInjectorsTest\$TargetStatic"
        const val TARGET_PLUGIN_ID_SKIP = "icu.windea.pls.inject.CodeInjectorsTest\$TargetPluginIdSkip"
        const val TARGET_INTERNAL_TO_STRING_MODEL = "icu.windea.pls.inject.CodeInjectorsTest\$InternalToStringModel"
        const val TARGET_FIELD_CACHE_MODEL = "icu.windea.pls.inject.CodeInjectorsTest\$FieldCacheModel"
        const val TARGET_OPTIMIZED_FIELD_MODEL = "icu.windea.pls.inject.CodeInjectorsTest\$OptimizedFieldModel"
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

    private fun findLoadedClass(classLoader: ClassLoader, className: String): Class<*>? {
        val m = ClassLoader::class.java.getDeclaredMethod("findLoadedClass", String::class.java)
        m.isAccessible = true
        return m.invoke(classLoader, className) as? Class<*>
    }

    private fun registerInjector(codeInjector: CodeInjector) {
        CodeInjectorScope.codeInjectors[codeInjector.id] = codeInjector
    }

    @Test
    fun test_baseSupport_before_continueInvocation() {
        makeTargetClass(
            TARGET_BEFORE,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_BEFORE, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BEFORE)
            fun before(a: Int, b: Int): Int {
                if (a == 0) continueInvocation()
                return a * b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_BEFORE, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(5, method.invoke(instance, 0, 5))
        assertEquals(6, method.invoke(instance, 2, 3))
    }

    @Test
    fun test_baseSupport_body_replace() {
        makeTargetClass(
            TARGET_BODY,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_BODY, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: Int, b: Int): Int {
                return a - b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_BODY, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(1, method.invoke(instance, 2, 1))
    }

    @Test
    fun test_baseSupport_after_useReturnValue() {
        makeTargetClass(
            TARGET_AFTER,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_AFTER, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.AFTER)
            fun after(a: Int, b: Int, returnValue: Int): Int {
                return returnValue + 1
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_AFTER, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(4, method.invoke(instance, 1, 2))
    }

    @Test
    fun test_baseSupport_afterFinally_useReturnValue() {
        makeTargetClass(
            TARGET_AFTER_FINALLY,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_AFTER_FINALLY, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.AFTER_FINALLY)
            fun afterFinally(a: Int, b: Int, returnValue: Int): Int {
                return returnValue + 2
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_AFTER_FINALLY, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(5, method.invoke(instance, 1, 2))
    }

    @Test
    fun test_baseSupport_staticMethod() {
        makeTargetClass(
            TARGET_STATIC,
            listOf(
                "public static int sumStatic(int a, int b) { return a + b; }"
            )
        )

        @InjectionTarget(TARGET_STATIC, pluginId = "icu.windea.pls")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sumStatic", pointer = InjectMethod.Pointer.BODY, static = true)
            fun sumStatic(a: Int, b: Int): Int {
                return a * b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_STATIC, false, this::class.java.classLoader)
        val method = clazz.getMethod("sumStatic", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(6, method.invoke(null, 2, 3))
    }

    @Test
    fun test_injector_pluginId_notEnabled_skip() {
        makeTargetClass(
            TARGET_PLUGIN_ID_SKIP,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )

        assertNull(findLoadedClass(javaClass.classLoader, TARGET_PLUGIN_ID_SKIP))

        @InjectionTarget(TARGET_PLUGIN_ID_SKIP, pluginId = "__not_enabled_plugin_id__")
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: Int, b: Int): Int {
                return a - b
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        assertNull(findLoadedClass(javaClass.classLoader, TARGET_PLUGIN_ID_SKIP))
    }

    @Test
    fun test_inject_internalModel_toString_after() {
        // 不要对任何生产类（平台/插件）做注入，以免污染全局行为影响其它测试。
        if (findLoadedClass(this::class.java.classLoader, TARGET_INTERNAL_TO_STRING_MODEL) != null) return

        @InjectionTarget(TARGET_INTERNAL_TO_STRING_MODEL)
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "toString", pointer = InjectMethod.Pointer.AFTER)
            fun after(returnValue: Any?): String {
                return "X:" + (returnValue?.toString() ?: "null")
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_INTERNAL_TO_STRING_MODEL, false, this::class.java.classLoader)
        val ctor = clazz.getDeclaredConstructor(Int::class.javaPrimitiveType)
        val instance = ctor.newInstance(1)
        assertTrue(instance.toString().startsWith("X:"))
    }

    @Test
    fun test_fieldCache_support_cache_and_cleanup() {
        if (findLoadedClass(this::class.java.classLoader, TARGET_FIELD_CACHE_MODEL) != null) return

        @InjectionTarget(TARGET_FIELD_CACHE_MODEL)
        @FieldCache("compute", cleanUp = "cleanUp")
        class Injector : CodeInjectorBase()

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_FIELD_CACHE_MODEL, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()

        val compute = clazz.getMethod("compute")
        val cleanUp = clazz.getMethod("cleanUp")
        val getComputeCount = clazz.getMethod("getComputeCount")

        assertEquals(1, compute.invoke(instance))
        assertEquals(1, compute.invoke(instance))
        assertEquals(1, getComputeCount.invoke(instance))

        cleanUp.invoke(instance)
        assertEquals(2, compute.invoke(instance))
        assertEquals(2, getComputeCount.invoke(instance))
    }

    @Test
    fun test_optimizedField_support_replaceType_and_init() {
        if (findLoadedClass(this::class.java.classLoader, TARGET_OPTIMIZED_FIELD_MODEL) != null) return

        @InjectionTarget(TARGET_OPTIMIZED_FIELD_MODEL)
        @OptimizedField(value = "value", type = OptimizedFieldNewType::class, initType = OptimizedFieldNewType::class)
        class Injector : CodeInjectorBase()

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_OPTIMIZED_FIELD_MODEL, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()

        val getKind = clazz.getMethod("getKind")
        assertEquals("new", getKind.invoke(instance))

        val field = clazz.getDeclaredField("value")
        assertEquals(OptimizedFieldNewType::class.java.name, field.type.name)
    }

    private class InternalToStringModel(private val v: Int) {
        override fun toString(): String = "V:$v"
    }

    private class FieldCacheModel {
        private var computeCount: Int = 0

        fun compute(): Int {
            computeCount++
            return computeCount
        }

        fun cleanUp() {
        }

        fun getComputeCount(): Int = computeCount
    }

    private open class OptimizedFieldOldType {
        open fun kind(): String = "old"
    }

    private class OptimizedFieldNewType : OptimizedFieldOldType() {
        override fun kind(): String = "new"
    }

    private class OptimizedFieldModel {
        private var value: OptimizedFieldOldType = OptimizedFieldOldType()

        fun getKind(): String = value.kind()
    }
}
