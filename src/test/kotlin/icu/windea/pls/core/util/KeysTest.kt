package icu.windea.pls.core.util

import com.intellij.openapi.util.UserDataHolderBase
import org.junit.Assert.*
import org.junit.Test

class KeysTest {
    private class Obj : UserDataHolderBase()

    private object Keys : KeyRegistry()

    @Test
    fun testCreateKey_nameAndIsolation() {
        val k1 = createKey<String>("k1")
        val k2 = createKey<String>("k2")
        assertNotSame(k1, k2)

        val o = Obj()
        o.putUserData(k1, "v1")
        o.putUserData(k2, "v2")
        assertEquals("v1", o.getUserData(k1))
        assertEquals("v2", o.getUserData(k2))
    }

    @Test
    fun testKeyClear() {
        val target = Obj()

        val k = createKey<String>("k")
        target.putUserData(k, "v")
        k.clear(target)
        assertNull(target.getUserData(k))
    }

    @Test
    fun testKeyCopy_basic() {
        val source = Obj()
        val target = Obj()

        val k = createKey<String>("k")
        source.putUserData(k, "v")
        k.copy(source, target)
        assertEquals("v", target.getUserData(k))
    }

    @Test
    fun testKeyCopy_ifPresentTrue_doesNotOverwriteWhenSourceMissing() {
        val source = Obj()
        val target = Obj()

        val k = createKey<String>("k")
        target.putUserData(k, "old")

        k.copy(source, target, ifPresent = true)
        assertEquals("old", target.getUserData(k))
    }

    @Test
    fun testKeyCopy_ifPresentFalse_clearsWhenSourceMissing() {
        val source = Obj()
        val target = Obj()

        val k = createKey<String>("k")
        target.putUserData(k, "old")

        k.copy(source, target, ifPresent = false)
        assertNull(target.getUserData(k))
    }

    @Test
    fun testKeyRegistry_idWithKeysSuffixRemoved() {
        assertEquals("KeysTest", Keys.id)
    }

    @Test
    fun testRegisterKey_delegateAndRegistryGetAndCaching() {
        class Registry : KeyRegistry() {
            val keyA by registerKey<String?>(this)
        }

        val registry = Registry()

        val keyA = registry.keyA
        val expectedName = registry.id + "." + "keyA"
        assertEquals(expectedName, keyA.name)

        val keyA2 = registry.keyA
        assertSame(keyA, keyA2)

        val fetched = registry.getKeyOrNull<String?>(expectedName)
        assertSame(keyA, fetched)
    }

    @Test
    fun testRegisterNamedKey_usesExactName() {
        class Registry : KeyRegistry() {
            val k by registerNamedKey<String>(this, "named.key")
        }

        val registry = Registry()
        val k = registry.k
        assertEquals("named.key", k.name)
    }

    @Test
    fun testRegisterKey_includeDefault_delegateAndDefaultValue() {
        class Registry : KeyRegistry() {
            val k by registerKey(this, "d")
        }

        val registry = Registry()
        val k = registry.k
        assertEquals(registry.id + ".k", k.name)
        assertEquals("d", k.default)
        assertSame(k, registry.k)
    }

    @Test
    fun testRegisterNamedKey_includeDefault_usesExactNameAndDefaultValue() {
        class Registry : KeyRegistry() {
            val k by registerNamedKey(this, "named.key", "d")
        }

        val registry = Registry()
        val k = registry.k
        assertEquals("named.key", k.name)
        assertEquals("d", k.default)
        assertSame(k, registry.k)
    }

    @Test
    fun testPropertyDelegates_commonUsages_comprehensive() {
        class RegistrySynced : KeyRegistry() {
            val nullableKey by registerKey<String?>(this)
            val defaultKey by registerKey(this, "d1")
            val factoryKey by registerKey<String, Obj>(this) { "f1" }
            val namedKey by registerNamedKey<Int>(this, "KeysTest.namedKey")
            val namedDefaultKey by registerNamedKey(this, "KeysTest.namedDefaultKey", "d2")
            val namedFactoryKey by registerNamedKey<String, Obj>(this, "KeysTest.namedFactoryKey") { "f2" }
        }

        val registry = RegistrySynced()

        val nullableKey = registry.nullableKey
        val defaultKey = registry.defaultKey
        val factoryKey = registry.factoryKey
        val namedKey = registry.namedKey
        val namedDefaultKey = registry.namedDefaultKey
        val namedFactoryKey = registry.namedFactoryKey

        assertEquals(registry.id + ".nullableKey", nullableKey.name)
        assertEquals(registry.id + ".defaultKey", defaultKey.name)
        assertEquals(registry.id + ".factoryKey", factoryKey.name)
        assertEquals("KeysTest.namedKey", namedKey.name)
        assertEquals("KeysTest.namedDefaultKey", namedDefaultKey.name)
        assertEquals("KeysTest.namedFactoryKey", namedFactoryKey.name)

        assertEquals("d1", defaultKey.default)
        assertEquals("d2", namedDefaultKey.default)

        assertSame(nullableKey, registry.nullableKey)
        assertSame(defaultKey, registry.defaultKey)
        assertSame(factoryKey, registry.factoryKey)
        assertSame(namedKey, registry.namedKey)
        assertSame(namedDefaultKey, registry.namedDefaultKey)
        assertSame(namedFactoryKey, registry.namedFactoryKey)

        assertTrue(registry.keys.containsKey(defaultKey.name))
        assertTrue(registry.keys.containsKey(namedDefaultKey.name))
    }

    @Test
    fun testKeyRegistryGetValue_operatorReturnsKeyItself() {
        val registry = object : KeyRegistry() {
            val k by registerKey<String>(this)
        }

        val key = registry.k
        assertSame(key, registry.k)
        assertSame(key, registry.keys.values.single())
    }
}
