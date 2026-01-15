package icu.windea.pls.inject

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.inject.annotations.InlinedDelegateField
import icu.windea.pls.inject.support.InlinedDelegateFieldCodeInjectorSupport
import javassist.ClassClassPath
import javassist.ClassPool
import org.junit.Assert.*
import org.junit.Test

class InlinedDelegateFieldCodeInjectorSupportTest {
    @Suppress("unused")
    class Model : UserDataHolderBase() {
        object Keys : KeyRegistry() {
            val name by registerKey<String, UserDataHolder>(this) { "" }
            val value by registerKey<Int>(this)
        }

        var name by Keys.name
        var value by Keys.value
    }

    @InlinedDelegateField(
        value = "name",
        delegateExpression = "icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$Model\$Keys.INSTANCE.getName()"
    )
    private class Injector : CodeInjector, UserDataHolderBase() {
        override val id: String = "InlinedDelegateFieldCodeInjectorSupportTest"

        override fun inject() {
        }
    }

    private class ByteArrayClassLoader(parent: ClassLoader) : ClassLoader(parent) {
        fun define(name: String, bytes: ByteArray): Class<*> {
            return defineClass(name, bytes, 0, bytes.size)
        }
    }

    @Test
    fun testInlineDelegateField() {
        val targetClassName = "icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$Model"

        val originalClass = Class.forName(targetClassName, false, javaClass.classLoader)
        assertTrue(originalClass.declaredFields.any { it.name == "name\$delegate" })

        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(javaClass))
        val ctClass = pool.get(targetClassName)
        ctClass.defrost()

        val injector = Injector()
        injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)

        InlinedDelegateFieldCodeInjectorSupport().apply(injector)

        val bytecode = ctClass.toBytecode()
        ctClass.detach()

        val loader = ByteArrayClassLoader(javaClass.classLoader)
        val injectedClass = loader.define(targetClassName, bytecode)

        assertFalse(injectedClass.declaredFields.any { it.name == "name\$delegate" })

        val instance = injectedClass.getDeclaredConstructor().newInstance()
        val getName = injectedClass.getMethod("getName")
        val setName = injectedClass.getMethod("setName", String::class.java)

        assertEquals("", getName.invoke(instance))
        setName.invoke(instance, "x")
        assertEquals("x", getName.invoke(instance))
    }
}
