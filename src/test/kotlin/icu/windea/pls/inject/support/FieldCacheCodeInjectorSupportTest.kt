package icu.windea.pls.inject.support

import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.CodeInjectorScope
import icu.windea.pls.inject.annotations.FieldCache
import icu.windea.pls.inject.annotations.InjectionTarget
import javassist.ClassClassPath
import javassist.ClassPool
import org.junit.Assert.*
import org.junit.Test

class FieldCacheCodeInjectorSupportTest {
    @Suppress("unused")
    class Model {
        @Volatile
        private var count = 0

        fun getAndInc(): Int {
            count++
            return count
        }

        fun cleanUp() {
        }
    }

    @FieldCache("getAndInc", cleanUp = "cleanUp")
    @InjectionTarget("icu.windea.pls.inject.support.FieldCacheCodeInjectorSupportTest\$Model")
    private class Injector : CodeInjectorBase()

    private class ByteArrayClassLoader(parent: ClassLoader) : ClassLoader(parent) {
        fun define(name: String, bytes: ByteArray): Class<*> {
            return defineClass(name, bytes, 0, bytes.size)
        }
    }

    @Test
    fun testFieldCache_andCleanup() {
        val targetClassName = "icu.windea.pls.inject.support.FieldCacheCodeInjectorSupportTest\$Model"

        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(javaClass))
        val ctClass = pool.get(targetClassName)
        ctClass.defrost()

        val injector = Injector()
        injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)

        FieldCacheCodeInjectorSupport().apply(injector)

        val bytecode = ctClass.toBytecode()
        ctClass.detach()

        val loader = ByteArrayClassLoader(javaClass.classLoader)
        val injectedClass = loader.define(targetClassName, bytecode)

        val instance = injectedClass.getDeclaredConstructor().newInstance()
        val getAndInc = injectedClass.getMethod("getAndInc")
        val cleanUp = injectedClass.getMethod("cleanUp")

        assertEquals(1, getAndInc.invoke(instance))
        assertEquals(1, getAndInc.invoke(instance))

        cleanUp.invoke(instance)

        assertEquals(2, getAndInc.invoke(instance))
        assertEquals(2, getAndInc.invoke(instance))
    }
}
