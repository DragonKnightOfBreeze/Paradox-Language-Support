package icu.windea.pls.core.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.util.ProcessingContext
import org.junit.Assert
import org.junit.Test

class KeyAccessorsTest {
    private class Obj : UserDataHolderBase()

    private object Keys : KeyRegistry() {
        val defaultKey by registerNamedKey(this, "KeyAccessorsTest.defaultKey", "d")

        val contextDefaultKey by registerNamedKey(this, "KeyAccessorsTest.contextDefaultKey", "cd")
        val contextFactoryKey by registerNamedKey<String, ProcessingContext>(this, "KeyAccessorsTest.contextFactoryKey") { "cf" }
        val contextNullableFactoryKey by registerNamedKey<String?, ProcessingContext>(this, "KeyAccessorsTest.contextNullableFactoryKey") { null }
    }

    private var ProcessingContext.defaultValue: String by Keys.defaultKey

    private var ProcessingContext.contextDefaultValue: String by Keys.contextDefaultKey

    private var ProcessingContext.contextFactoryValue: String by Keys.contextFactoryKey

    private var ProcessingContext.contextNullableFactoryValue: String? by Keys.contextNullableFactoryKey


    @Test
    fun testGetOrPutUserData_keyBased_valueAndNullDefaultAreCached() {
        val obj = Obj()
        val k1: Key<String> = createKey("k1")
        val k2: Key<String> = createKey("k2")

        var count = 0
        Assert.assertEquals(null, obj.getUserData(k1))
        Assert.assertEquals("value", obj.getOrPutUserData(k1) { count++; "value" })
        Assert.assertEquals(1, count)
        Assert.assertEquals("value", obj.getOrPutUserData(k1) { count++; "other" })
        Assert.assertEquals(1, count)

        Assert.assertEquals(null, obj.getUserData(k2))
        Assert.assertEquals(null, obj.getOrPutUserData<String?>(k2) { count++; null })
        Assert.assertEquals(2, count)
        Assert.assertEquals(null, obj.getOrPutUserData<String?>(k2) { count++; "value" })
        Assert.assertEquals(2, count)
    }

    @Test
    fun testGetOrPutUserData_registedKey_withoutFactory_returnsNullAndDoesNotStore() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey<String?>(registry, "k").getKey()

        val obj = Obj()
        Assert.assertNull(obj.getOrPutUserData(key))
        Assert.assertNull(obj.getUserData(key))
    }

    @Test
    fun testGetOrPutUserData_registedKeyWithFactory_valueCached() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey<String, Obj>(registry, "k") { "v" }.getKey()

        val obj = Obj()
        Assert.assertEquals("v", obj.getOrPutUserData(key))
        Assert.assertEquals("v", obj.getOrPutUserData(key))
        Assert.assertEquals("v", obj.getUserData(key))
    }

    @Test
    fun testGetOrPutUserData_registedKeyWithFactory_nullValueCachedWithEmptyObject() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey<String?, Obj>(registry, "k") { null }.getKey()

        val obj = Obj()
        Assert.assertEquals(null, obj.getOrPutUserData(key))
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, obj.getUserData(key))
        Assert.assertEquals(null, obj.getOrPutUserData(key))
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, obj.getUserData(key))
    }

    @Test
    fun testRegistedKey_propertyDelegateReadWriteOnUserDataHolder() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey<String?>(registry, "k").getKey()

        class Holder : UserDataHolderBase() {
            var value: String? by key
        }

        val h = Holder()
        Assert.assertNull(h.value)

        h.value = "v"
        Assert.assertEquals("v", h.value)

        h.value = null
        Assert.assertNull(h.value)
        Assert.assertNull(h.getUserData(key))
    }

    @Test
    fun testRegistedKeyWithFactory_propertyDelegateReadOnUserDataHolder() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey<String, UserDataHolderBase>(registry, "k") { "v" }.getKey()

        class Holder : UserDataHolderBase() {
            val value: String by key
        }

        val h = Holder()
        Assert.assertEquals("v", h.value)
        Assert.assertEquals("v", h.value)
        Assert.assertEquals("v", h.getUserData(key))
    }

    @Test
    fun testGetOrPutUserData_registedKeyWithDefault_returnsDefaultAndDoesNotStore() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey(registry, "k", "d").getKey()

        val obj = Obj()
        Assert.assertEquals("d", obj.getOrPutUserData(key))
        Assert.assertNull(obj.getUserData(key))

        @Suppress("UNCHECKED_CAST")
        obj.putUserData(key as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("d", obj.getOrPutUserData(key))
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, obj.getUserData(key))

        obj.putUserData(key, "x")
        Assert.assertEquals("x", obj.getOrPutUserData(key))
    }

    @Test
    fun testGetOrPutUserData_registedKeyWithDefault_asRegistedKeyOverload() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey(registry, "k", "d").getKey()

        val obj = Obj()
        Assert.assertEquals("d", obj.getOrPutUserData(key as RegistedKey<String>))
        Assert.assertNull(obj.getUserData(key))
    }

    @Test
    fun testRegistedKeyWithDefault_propertyDelegateOnUserDataHolder() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey(registry, "k", "d").getKey()

        class Holder : UserDataHolderBase() {
            var value: String by key
        }

        val h = Holder()
        Assert.assertEquals("d", h.value)
        Assert.assertNull(h.getUserData(key))

        h.value = "x"
        Assert.assertEquals("x", h.value)

        @Suppress("UNCHECKED_CAST")
        h.putUserData(key as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("d", h.value)
    }

    @Test
    fun testProcessingContext_getOrPut_registedKeyAndFactoryAndNullCaching() {
        val registry = object : KeyRegistry() {}
        val key1 = registerNamedKey<String?>(registry, "k1").getKey()
        val key2 = registerNamedKey<String, ProcessingContext>(registry, "k2") { "v" }.getKey()
        val key3 = registerNamedKey<String?, ProcessingContext>(registry, "k3") { null }.getKey()

        val context = ProcessingContext()
        Assert.assertNull(context.getOrPut(key1))
        Assert.assertNull(context.get(key1))

        Assert.assertEquals("v", context.getOrPut(key2))
        Assert.assertEquals("v", context.getOrPut(key2))
        Assert.assertEquals("v", context.get(key2))

        Assert.assertNull(context.getOrPut(key3))
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, context.get(key3))
        Assert.assertNull(context.getOrPut(key3))
    }

    @Test
    fun testProcessingContext_getOrPut_registedKeyWithDefault_returnsDefaultAndDoesNotStore() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey(registry, "k", "d").getKey()

        val context = ProcessingContext()
        Assert.assertEquals("d", context.getOrPut(key))
        Assert.assertNull(context.get(key))

        @Suppress("UNCHECKED_CAST")
        context.put(key as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("d", context.getOrPut(key))
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, context.get(key))

        context.put(key, "x")
        Assert.assertEquals("x", context.getOrPut(key))
    }

    @Test
    fun testProcessingContext_getOrPut_registedKeyWithDefault_asRegistedKeyOverload() {
        val registry = object : KeyRegistry() {}
        val key = registerNamedKey(registry, "k", "d").getKey()

        val context = ProcessingContext()
        Assert.assertEquals("d", context.getOrPut(key as RegistedKey<String>))
        Assert.assertNull(context.get(key))
    }

    @Test
    fun testRegistedKeyWithDefault_propertyDelegateOnProcessingContext() {
        val context = ProcessingContext()
        Assert.assertEquals("d", context.defaultValue)
        Assert.assertNull(context.get(Keys.defaultKey))

        context.defaultValue = "x"
        Assert.assertEquals("x", context.defaultValue)

        @Suppress("UNCHECKED_CAST")
        context.put(Keys.defaultKey as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("d", context.defaultValue)
    }

    @Test
    fun testPropertyDelegates_commonUsages_comprehensive() {
        class Registry : KeyRegistryWithSync() {
            val nullableKey by registerKey<String?>(this)
            val defaultKey by registerKey(this, "d").withSync()
            val factoryKey by registerKey<Int, UserDataHolderBase>(this) { 42 }
            val namedKey by registerNamedKey<String?>(this, "KeyAccessorsTest.namedKey")
            val namedDefaultKey by registerNamedKey(this, "KeyAccessorsTest.namedDefaultKey", "nd")
            val namedFactoryKey by registerNamedKey<String, UserDataHolderBase>(this, "KeyAccessorsTest.namedFactoryKey") { "nf" }
        }

        val registry = Registry()

        class Holder : UserDataHolderBase() {
            var a: String? by registry.nullableKey
            var b: String by registry.defaultKey
            val c: Int by registry.factoryKey
            var d: String? by registry.namedKey
            var e: String by registry.namedDefaultKey
            val f: String by registry.namedFactoryKey
        }

        val h = Holder()

        // UserDataHolder 成员属性委托：默认值/工厂/可空
        Assert.assertNull(h.a)
        Assert.assertEquals("d", h.b)
        Assert.assertEquals(42, h.c)
        Assert.assertNull(h.d)
        Assert.assertEquals("nd", h.e)
        Assert.assertEquals("nf", h.f)

        // 默认值 key 不会写入 userData；工厂 key 会缓存
        Assert.assertNull(h.getUserData(registry.defaultKey))
        Assert.assertNull(h.getUserData(registry.namedDefaultKey))
        Assert.assertEquals(42, h.getUserData(registry.factoryKey))
        Assert.assertEquals("nf", h.getUserData(registry.namedFactoryKey))

        h.a = "a1"
        h.b = "b1"
        h.d = "d1"
        h.e = "e1"

        Assert.assertEquals("a1", h.a)
        Assert.assertEquals("b1", h.b)
        Assert.assertEquals("d1", h.d)
        Assert.assertEquals("e1", h.e)

        h.a = null
        Assert.assertNull(h.a)
        Assert.assertNull(h.getUserData(registry.nullableKey))

        @Suppress("UNCHECKED_CAST")
        h.putUserData(registry.defaultKey as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("d", h.b)

        // ProcessingContext 扩展属性委托：默认值/工厂/可空
        val context = ProcessingContext()
        Assert.assertEquals("cd", context.contextDefaultValue)
        Assert.assertEquals("cf", context.contextFactoryValue)
        Assert.assertNull(context.contextNullableFactoryValue)

        context.contextDefaultValue = "cd1"
        context.contextFactoryValue = "cf1"
        Assert.assertEquals("cd1", context.contextDefaultValue)
        Assert.assertEquals("cf1", context.contextFactoryValue)

        // default key 读取默认值时不落盘，但通过委托赋值会写入；factory key 会缓存
        Assert.assertEquals("cd1", context.get(Keys.contextDefaultKey))
        Assert.assertEquals("cf1", context.get(Keys.contextFactoryKey))

        @Suppress("UNCHECKED_CAST")
        context.put(Keys.contextDefaultKey as Key<Any>, icu.windea.pls.core.EMPTY_OBJECT)
        Assert.assertEquals("cd", context.contextDefaultValue)
        Assert.assertSame(icu.windea.pls.core.EMPTY_OBJECT, context.get(Keys.contextDefaultKey))
    }
}
