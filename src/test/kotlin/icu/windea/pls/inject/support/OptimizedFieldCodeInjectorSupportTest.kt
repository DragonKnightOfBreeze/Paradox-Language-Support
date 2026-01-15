package icu.windea.pls.inject.support

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.OptimizedField
import javassist.ClassClassPath
import javassist.ClassPool
import org.junit.Assert.*
import org.junit.Test

class OptimizedFieldCodeInjectorSupportTest {
    @Suppress("unused")
    open class OldType {
        fun plusOne(v: Int): Int = v + 1
    }

    @Suppress("unused")
    class NewType : OldType()

    @Suppress("unused")
    class Model {
        private var field: OldType = OldType()

        fun compute(v: Int): Int {
            return field.plusOne(v)
        }

        fun getFieldClassName(): String {
            return field.javaClass.name
        }
    }

    @OptimizedField("field", type = NewType::class, initType = NewType::class)
    @InjectionTarget("icu.windea.pls.inject.support.OptimizedFieldCodeInjectorSupportTest\$Model")
    private class Injector : CodeInjectorBase()

    private class ByteArrayClassLoader(parent: ClassLoader) : ClassLoader(parent) {
        fun define(name: String, bytes: ByteArray): Class<*> {
            return defineClass(name, bytes, 0, bytes.size)
        }
    }

    @Test
    fun testOptimizedField_replacesFieldTypeAndKeepsBehavior() {
        val targetClassName = "icu.windea.pls.inject.support.OptimizedFieldCodeInjectorSupportTest\$Model"

        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(javaClass))
        val ctClass = pool.get(targetClassName)
        ctClass.defrost()

        CodeInjectorScope.classPool = pool
        try {
            val injector = Injector()
            injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)

            OptimizedFieldCodeInjectorSupport().apply(injector)

            val bytecode = ctClass.toBytecode()
            ctClass.detach()

            val loader = ByteArrayClassLoader(javaClass.classLoader)
            val injectedClass = loader.define(targetClassName, bytecode)

            val instance = injectedClass.getDeclaredConstructor().newInstance()
            val compute = injectedClass.getMethod("compute", Int::class.javaPrimitiveType)
            val getFieldClassName = injectedClass.getMethod("getFieldClassName")

            assertEquals(2, compute.invoke(instance, 1))
            assertEquals(NewType::class.java.name, getFieldClassName.invoke(instance))
        } finally {
            CodeInjectorScope.classPool = null
        }
    }
}
