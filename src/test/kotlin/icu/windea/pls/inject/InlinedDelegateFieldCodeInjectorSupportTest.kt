package icu.windea.pls.inject

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.setValue
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.inject.annotations.InlinedDelegateField
import icu.windea.pls.inject.annotations.InlinedDelegateFields
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

    @Suppress("unused")
    class Model2 : UserDataHolderBase() {
        object Keys : KeyRegistry() {
            val name by registerKey<String, UserDataHolder>(this) { "" }
            val value by registerKey<Int, UserDataHolder>(this) { 0 }
        }

        var name by Keys.name
        var value by Keys.value
    }

    @Suppress("unused")
    class ModelFail : UserDataHolderBase() {
        object Keys : KeyRegistry() {
            val name by registerKey<String, UserDataHolder>(this) { "" }
        }

        private fun nameDelegate() = Keys.name

        var name by nameDelegate()
    }

    @InlinedDelegateField(value = "name")
    @InjectionTarget("icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$Model")
    private class Injector : CodeInjectorBase()

    @InlinedDelegateFields
    @InjectionTarget("icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$Model2")
    private class Injector2 : CodeInjectorBase()

    @InlinedDelegateField(value = "name")
    @InjectionTarget("icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$ModelFail")
    private class InjectorFail : CodeInjectorBase()

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

    @Test
    fun testInlineDelegateField_inferFailed_skipped() {
        val targetClassName = "icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$ModelFail"

        val originalClass = Class.forName(targetClassName, false, javaClass.classLoader)
        assertTrue(originalClass.declaredFields.any { it.name == "name\$delegate" })

        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(javaClass))
        val ctClass = pool.get(targetClassName)
        ctClass.defrost()

        val injector = InjectorFail()
        injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)

        InlinedDelegateFieldCodeInjectorSupport().apply(injector)

        val bytecode = ctClass.toBytecode()
        ctClass.detach()

        val loader = ByteArrayClassLoader(javaClass.classLoader)
        val injectedClass = loader.define(targetClassName, bytecode)

        assertTrue(injectedClass.declaredFields.any { it.name == "name\$delegate" })

        val instance = injectedClass.getDeclaredConstructor().newInstance()
        val getName = injectedClass.getMethod("getName")
        val setName = injectedClass.getMethod("setName", String::class.java)
        assertEquals("", getName.invoke(instance))
        setName.invoke(instance, "x")
        assertEquals("x", getName.invoke(instance))
    }

    @Test
    fun testInlineDelegateFields_inlineAll() {
        val targetClassName = "icu.windea.pls.inject.InlinedDelegateFieldCodeInjectorSupportTest\$Model2"

        val originalClass = Class.forName(targetClassName, false, javaClass.classLoader)
        assertTrue(originalClass.declaredFields.any { it.name == "name\$delegate" })
        assertTrue(originalClass.declaredFields.any { it.name == "value\$delegate" })

        val pool = ClassPool.getDefault()
        pool.appendClassPath(ClassClassPath(javaClass))
        val ctClass = pool.get(targetClassName)
        ctClass.defrost()

        val injector = Injector2()
        injector.putUserData(CodeInjectorScope.targetClassKey, ctClass)

        InlinedDelegateFieldCodeInjectorSupport().apply(injector)

        val bytecode = ctClass.toBytecode()
        ctClass.detach()

        val loader = ByteArrayClassLoader(javaClass.classLoader)
        val injectedClass = loader.define(targetClassName, bytecode)

        assertFalse(injectedClass.declaredFields.any { it.name == "name\$delegate" })
        assertFalse(injectedClass.declaredFields.any { it.name == "value\$delegate" })

        val instance = injectedClass.getDeclaredConstructor().newInstance()

        val getName = injectedClass.getMethod("getName")
        val setName = injectedClass.getMethod("setName", String::class.java)
        assertEquals("", getName.invoke(instance))
        setName.invoke(instance, "x")
        assertEquals("x", getName.invoke(instance))

        val getValue = injectedClass.getMethod("getValue")
        val setValue = injectedClass.getMethod("setValue", Int::class.javaPrimitiveType)
        assertEquals(0, getValue.invoke(instance))
        setValue.invoke(instance, 1)
        assertEquals(1, getValue.invoke(instance))
    }
}
