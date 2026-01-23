package icu.windea.pls.inject.injectors

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.inject.CodeInjector
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.SuperMethodInvoker
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectSuperMethod
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
class CodeInjectorSuperMethodTest : BasePlatformTestCase() {
    private companion object {
        const val TARGET_BASE = "icu.windea.pls.inject.injectors.CodeInjectorSuperMethodTest\$TargetBase"
        const val TARGET_DERIVED = "icu.windea.pls.inject.injectors.CodeInjectorSuperMethodTest\$TargetDerived"
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

    private fun registerInjector(codeInjector: CodeInjector) {
        CodeInjectorScope.codeInjectors[codeInjector.id] = codeInjector
    }

    private fun makeTargetClass(className: String, methods: List<String>, superClassName: String? = null): CtClass {
        val pool = CodeInjectorScope.classPool ?: error("ClassPool is not initialized")
        val ctClass = pool.makeClass(className)
        if (superClassName != null) {
            ctClass.superclass = pool.get(superClassName)
        }
        ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass))
        methods.forEach { ctClass.addMethod(CtNewMethod.make(it, ctClass)) }
        ctClass.stopPruning(true)
        return ctClass
    }

    @Test
    fun test_superMethodInvoker_callsSuperImplementation() {
        val baseCtClass = makeTargetClass(
            TARGET_BASE,
            listOf(
                "public int sum(int a, int b) { return a + b; }"
            )
        )
        baseCtClass.toClass()

        makeTargetClass(
            TARGET_DERIVED,
            listOf(
                "public int sum(int a, int b) { return a * b; }"
            ),
            superClassName = TARGET_BASE
        )

        @InjectionTarget(TARGET_DERIVED)
        class Injector : CodeInjectorBase() {
            @InjectMethod(value = "sum", pointer = InjectMethod.Pointer.BODY)
            fun sum(a: Int, b: Int, @InjectSuperMethod superCall: SuperMethodInvoker): Int {
                val superResult = (superCall.invoke() as Number).toInt()
                return superResult + 1
            }
        }

        val injector = Injector()
        injector.inject()
        registerInjector(injector)

        val clazz = Class.forName(TARGET_DERIVED, false, this::class.java.classLoader)
        val instance = clazz.getDeclaredConstructor().newInstance()
        val method = clazz.getMethod("sum", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
        assertEquals(4, method.invoke(instance, 1, 2))
    }
}
