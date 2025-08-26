package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KFunction

class ReflectionExtensionsTest {
    class Foo {
        private var _foo: String? = null
        private var _active: Boolean = false

        fun getFoo(): String? = _foo
        fun setFoo(v: String?) { _foo = v }

        fun isActive(): Boolean = _active
        fun setActive(v: Boolean) { _active = v }

        // Non-accessor methods
        fun getValueX() = 1
        fun setValueX(x: Int) {}
    }

    open class GenericHolder : ArrayList<String>()

    @Test
    fun isGetter_isSetter_without_propertyName() {
        val k = Foo::class
        val getFoo = k.members.first { it.name == "getFoo" } as KFunction<*>
        val setFoo = k.members.first { it.name == "setFoo" } as KFunction<*>
        val isActive = k.members.first { it.name == "isActive" } as KFunction<*>

        Assert.assertTrue(getFoo.isGetter())
        Assert.assertTrue(isActive.isGetter())
        Assert.assertTrue(setFoo.isSetter())

        val getValueX = k.members.first { it.name == "getValueX" } as KFunction<*>
        val setValueX = k.members.first { it.name == "setValueX" } as KFunction<*>
        Assert.assertTrue(getValueX.isGetter())
        Assert.assertTrue(setValueX.isSetter())
    }

    @Test
    fun isGetter_isSetter_with_propertyName() {
        val k = Foo::class
        val getFoo = k.members.first { it.name == "getFoo" } as KFunction<*>
        val setFoo = k.members.first { it.name == "setFoo" } as KFunction<*>
        val isActive = k.members.first { it.name == "isActive" } as KFunction<*>

        Assert.assertTrue(getFoo.isGetter("foo"))
        Assert.assertTrue(isActive.isGetter("active"))
        Assert.assertTrue(setFoo.isSetter("foo"))

        Assert.assertFalse(getFoo.isGetter("bar"))
        Assert.assertFalse(setFoo.isSetter("bar"))
    }

    @Test
    fun genericType_from_parameterizedType() {
        val type = GenericHolder::class.java.genericSuperclass // ArrayList<String>
        val arg0 = type.genericType<Class<*>>(0)
        Assert.assertEquals(String::class.java, arg0)
    }
}
