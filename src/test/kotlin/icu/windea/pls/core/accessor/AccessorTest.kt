package icu.windea.pls.core.accessor

import org.junit.*
import java.util.*

/**
 * @see Accessor
 */
class AccessorTest {
    // 1. 不能直接测试 Java 类，否则可能报错：java.lang.reflect.InaccessibleObjectException
    // 2. Kotlin 类的伴生对象的私有成员，不视为该类的静态成员

    val obj = AccessorObject("Windea", "Female", 24)
    val arg = "my dragon"
    val args = arrayOf("the companions", "the lands", "and the world")

    @Test
    fun testProperty_get() {
        Assert.assertEquals(obj.name, AccessorBuilder.property<_, String>(obj, "name", AccessorObject::class).get())
        Assert.assertEquals(obj.gender, AccessorBuilder.property<_, String>(obj, "gender", AccessorObject::class).get())
        Assert.assertEquals(obj.age, AccessorBuilder.property<_, Int>(obj, "age", AccessorObject::class).get())
        Assert.assertEquals(obj.text, AccessorBuilder.property<_, String>(obj, "text", AccessorObject::class).get())
        Assert.assertEquals(obj.description, AccessorBuilder.property<_, String>(obj, "description", AccessorObject::class).get())
        Assert.assertTrue(AccessorBuilder.property<_, Boolean>(obj, "awakenStatus", AccessorObject::class).get())
        Assert.assertEquals(obj.introduce, AccessorBuilder.property<_, String>(obj, "introduce", AccessorObject::class).get())
    }

    @Test
    fun testProperty_set() {
        AccessorBuilder.property<_, String>(obj, "introduce", AccessorObject::class).set("The dragon knight who with the title of breeze.")
        Assert.assertEquals("The dragon knight who with the title of breeze.", obj.introduce)
    }

    @Test
    fun testMemberProperty_get() {
        Assert.assertEquals(obj.name, AccessorBuilder.memberProperty<_, String>("name", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.gender, AccessorBuilder.memberProperty<_, String>("gender", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.age, AccessorBuilder.memberProperty<_, Int>("age", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.text, AccessorBuilder.memberProperty<_, String>("text", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.description, AccessorBuilder.memberProperty<_, String>("description", AccessorObject::class).get(obj))
        Assert.assertTrue(AccessorBuilder.memberProperty<_, Boolean>("awakenStatus", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.introduce, AccessorBuilder.memberProperty<_, String>("introduce", AccessorObject::class).get(obj))
    }

    @Test
    fun testMemberProperty_set() {
        AccessorBuilder.memberProperty<_, String>("introduce", AccessorObject::class).set(obj, "The dragon knight who with the title of breeze.")
        Assert.assertEquals("The dragon knight who with the title of breeze.", obj.introduce)
    }

    @Test
    fun testStaticProperty_get() {
        Assert.assertTrue(AccessorBuilder.staticProperty<_, Boolean>("initializedStatus", AccessorObject::class).get())
        Assert.assertEquals(AccessorObject.information, AccessorBuilder.staticProperty<_, String>("information", AccessorObject::class).get())
    }

    @Test
    fun testStatisProperty_set() {
        AccessorBuilder.staticProperty<_, String>("information", AccessorObject::class).set("Seeking...")
        Assert.assertEquals("Seeking...", AccessorObject.information)
    }

    @Test
    fun testFunction_call() {
        Assert.assertEquals(obj.helloWorld(), AccessorBuilder.function(obj, "helloWorld", AccessorObject::class).call())
        Assert.assertEquals(obj.hello(arg), AccessorBuilder.function(obj, "hello", AccessorObject::class).call(arg))
        Assert.assertEquals(obj.helloAll(*args), AccessorBuilder.function(obj, "helloAll", AccessorObject::class).call(args))

        Assert.assertEquals(obj.awake(), AccessorBuilder.function(obj, "awake", AccessorObject::class).call())
        Assert.assertEquals(obj.awake(), AccessorBuilder.function(obj, "doAwake", AccessorObject::class).call())
    }

    @Test
    fun testMemberFunction_call() {
        Assert.assertEquals(obj.helloWorld(), AccessorBuilder.memberFunction("helloWorld", AccessorObject::class).call(obj))
        Assert.assertEquals(obj.hello(arg), AccessorBuilder.memberFunction("hello", AccessorObject::class).call(obj, arg))
        Assert.assertEquals(obj.helloAll(*args), AccessorBuilder.memberFunction("helloAll", AccessorObject::class).call(obj, args))

        Assert.assertEquals(obj.awake(), AccessorBuilder.memberFunction("awake", AccessorObject::class).call(obj))
        Assert.assertEquals(obj.awake(), AccessorBuilder.memberFunction("doAwake", AccessorObject::class).call(obj))
    }

    @Test
    fun testStaticFunction_call() {
        Assert.assertEquals(AccessorObject.greetings(), AccessorBuilder.staticFunction("greetings", AccessorObject::class).call())
        Assert.assertEquals(AccessorObject.greetings(arg), AccessorBuilder.staticFunction("greetings", AccessorObject::class).call(arg))
        Assert.assertEquals(AccessorObject.greetingsAll(*args), AccessorBuilder.staticFunction("greetingsAll", AccessorObject::class).call(args))

        Assert.assertEquals(AccessorObject.initialize(), AccessorBuilder.staticFunction("initialize", AccessorObject::class).call())
        Assert.assertThrows(UnsupportedAccessorException::class.java) { AccessorBuilder.staticFunction("doInitialize", AccessorObject::class).call() } // not static function
    }
}
