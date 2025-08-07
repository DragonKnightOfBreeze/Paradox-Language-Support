package icu.windea.pls.core.util.accessor

import org.junit.*
import java.util.*

class AccessorTest {
    // 1. 不能直接测试 Java 类，否则可能报错：java.lang.reflect.InaccessibleObjectException
    // 2. Kotlin 类的伴生对象的私有成员，不视为该类的静态成员

    val obj = AccessorObject("Windea", "Female", "Ancient Sprite", 10000)
    val arg = "the seeker"
    val args = arrayOf("the seeker", "the ranger", "and all my pals")

    @Test
    fun testProperty() {
        Assert.assertEquals(obj.name, AccessorBuilder.property<_, String>(obj, "name", AccessorObject::class).get())
        Assert.assertEquals(obj.displayName, AccessorBuilder.property<_, String>(obj, "displayName", AccessorObject::class).get())
        Assert.assertEquals(obj.description, AccessorBuilder.property<_, String>(obj, "description", AccessorObject::class).get())

        Assert.assertTrue(AccessorBuilder.property<_, Boolean>(obj, "awakenStatus", AccessorObject::class).get())
    }

    @Test
    fun testMemberProperty() {
        Assert.assertEquals(obj.name, AccessorBuilder.memberProperty<_, String>("name", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.displayName, AccessorBuilder.memberProperty<_, String>("displayName", AccessorObject::class).get(obj))
        Assert.assertEquals(obj.description, AccessorBuilder.memberProperty<_, String>("description", AccessorObject::class).get(obj))

        Assert.assertTrue(AccessorBuilder.memberProperty<_, Boolean>("awakenStatus", AccessorObject::class).get(obj))
    }

    @Test
    fun testStaticProperty() {
        Assert.assertEquals(AccessorObject.initializedStatus, AccessorBuilder.staticProperty<_, Boolean>("initializedStatus", AccessorObject::class).get())
    }

    @Test
    fun testFunction() {
        Assert.assertEquals(obj.helloWorld(), AccessorBuilder.function(obj, "helloWorld", AccessorObject::class).invoke())
        Assert.assertEquals(obj.hello(arg), AccessorBuilder.function(obj, "hello", AccessorObject::class).invoke(arg))
        Assert.assertEquals(obj.helloAll(*args), AccessorBuilder.function(obj, "helloAll", AccessorObject::class).invoke(args))

        Assert.assertEquals(obj.awake(), AccessorBuilder.function(obj, "awake", AccessorObject::class).invoke())
        Assert.assertEquals(obj.awake(), AccessorBuilder.function(obj, "doAwake", AccessorObject::class).invoke())
    }

    @Test
    fun testMemberFunction() {
        Assert.assertEquals(obj.helloWorld(), AccessorBuilder.memberFunction("helloWorld", AccessorObject::class).invoke(obj))
        Assert.assertEquals(obj.hello(arg), AccessorBuilder.memberFunction("hello", AccessorObject::class).invoke(obj, arg))
        Assert.assertEquals(obj.helloAll(*args), AccessorBuilder.memberFunction("helloAll", AccessorObject::class).invoke(obj, args))

        Assert.assertEquals(obj.awake(), AccessorBuilder.memberFunction("awake", AccessorObject::class).invoke(obj))
        Assert.assertEquals(obj.awake(), AccessorBuilder.memberFunction("doAwake", AccessorObject::class).invoke(obj))
    }

    @Test
    fun testStaticFunction() {
        Assert.assertEquals(AccessorObject.initialize(), AccessorBuilder.staticFunction("initialize", AccessorObject::class).invoke())
    }
}
