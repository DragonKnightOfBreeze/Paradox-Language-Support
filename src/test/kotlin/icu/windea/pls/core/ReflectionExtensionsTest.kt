package icu.windea.pls.core

import org.junit.Assert
import org.junit.Test
import kotlin.reflect.KFunction

class ReflectionExtensionsTest {
    @Suppress("unused")
    class Foo {
        private var _foo: String? = null
        private var _active: Boolean = false

        fun getFoo(): String? = _foo
        fun setFoo(v: String?) {
            _foo = v
        }

        fun isActive(): Boolean = _active
        fun setActive(v: Boolean) {
            _active = v
        }

        // Non-accessor methods
        fun getValueX() = 1
        fun setValueX(x: Int) {}
    }

    open class GenericHolder : ArrayList<String>()

    @Test
    fun isGetter_isSetter_without_propertyName_test() {
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
    fun isGetter_isSetter_with_propertyName_test() {
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
    fun genericType_from_parameterizedType_test() {
        val type = GenericHolder::class.java.genericSuperclass // ArrayList<String>
        val arg0 = type.genericType<Class<*>>(0)
        Assert.assertEquals(String::class.java, arg0)
    }

    // region 类名解析

    // （来自鲸鱼）注意：
    // `Class.forName("kotlin.Int")` 在测试环境的 `PathClassLoader` 下无法解析（`isClassPresent("kotlin.Int")` 返回 `false`），已改用 `java.util.ArrayList` 等标准 JDK 类作为正例。
    // 这说明 `isClassPresent`/`toClass`/`toKClass` 对 Kotlin 内建类型名（`kotlin.Int` 等）在该类加载器下不可靠，若后续有此类需求需注意。

    @Test
    fun isClassPresent_test() {
        Assert.assertTrue("java.lang.String".isClassPresent())
        Assert.assertTrue("java.util.ArrayList".isClassPresent())
        Assert.assertFalse("nonexistent.NonExistentClass".isClassPresent())
    }

    @Test
    fun toClass_test() {
        Assert.assertEquals(String::class.java, "java.lang.String".toClass())
    }

    @Test
    fun toKClass_test() {
        Assert.assertEquals(String::class, "java.lang.String".toKClass())
    }

    // endregion

    // region isGetter / isSetter 负向用例

    @Test
    fun isGetter_isSetter_negative_test() {
        val k = Foo::class
        val toString = k.members.first { it.name == "toString" } as KFunction<*>
        val equals = k.members.first { it.name == "equals" } as KFunction<*>
        Assert.assertFalse(toString.isGetter())
        Assert.assertFalse(toString.isSetter())
        Assert.assertFalse(equals.isGetter()) // 参数数量不匹配
        Assert.assertFalse(equals.isSetter()) // 命名不匹配
    }

    // endregion
}
