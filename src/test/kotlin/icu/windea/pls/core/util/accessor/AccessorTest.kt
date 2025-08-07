package icu.windea.pls.core.util.accessor

import org.junit.*
import java.util.*

class AccessorTest {
    // 1. 不能直接测试 Java 类，否则可能报错：java.lang.reflect.InaccessibleObjectException
    // 2. Kotlin 类的伴生对象的私有成员，不视为该类的静态成员

    val target = TestObject("Windea", "Female", "Ancient Sprite", 10000)
    val arg = "the seeker"
    val args = arrayOf("the seeker", "the ranger", "and all my pals")

    @Test
    fun testProperty() {
        Assert.assertEquals(target.name, AccessorBuilder.property<_, String>(target, "name", TestObject::class).get())
        Assert.assertEquals(target.displayName, AccessorBuilder.property<_, String>(target, "displayName", TestObject::class).get())
        Assert.assertEquals(target.description, AccessorBuilder.property<_, String>(target, "description", TestObject::class).get())

        Assert.assertTrue(AccessorBuilder.property<_, Boolean>(target, "awakenStatus", TestObject::class).get())
    }

    @Test
    fun testMemberProperty() {
        Assert.assertEquals(target.name, AccessorBuilder.memberProperty<_, String>("name", TestObject::class).get(target))
        Assert.assertEquals(target.displayName, AccessorBuilder.memberProperty<_, String>("displayName", TestObject::class).get(target))
        Assert.assertEquals(target.description, AccessorBuilder.memberProperty<_, String>("description", TestObject::class).get(target))

        Assert.assertTrue(AccessorBuilder.memberProperty<_, Boolean>("awakenStatus", TestObject::class).get(target))
    }

    @Test
    fun testStaticProperty() {
        Assert.assertEquals(TestObject.initializedStatus, AccessorBuilder.staticProperty<_, Boolean>("initializedStatus", TestObject::class).get())
    }

    @Test
    fun testFunction() {
        Assert.assertEquals(target.helloWorld(), AccessorBuilder.function(target, "helloWorld", TestObject::class).invoke())
        Assert.assertEquals(target.hello(arg), AccessorBuilder.function(target, "hello", TestObject::class).invoke(arg))
        Assert.assertEquals(target.helloAll(*args), AccessorBuilder.function(target, "helloAll", TestObject::class).invoke(args))

        Assert.assertEquals(target.awake(), AccessorBuilder.function(target, "awake", TestObject::class).invoke())
        Assert.assertEquals(target.awake(), AccessorBuilder.function(target, "doAwake", TestObject::class).invoke())
    }

    @Test
    fun testMemberFunction() {
        Assert.assertEquals(target.helloWorld(), AccessorBuilder.memberFunction("helloWorld", TestObject::class).invoke(target))
        Assert.assertEquals(target.hello(arg), AccessorBuilder.memberFunction("hello", TestObject::class).invoke(target, arg))
        Assert.assertEquals(target.helloAll(*args), AccessorBuilder.memberFunction("helloAll", TestObject::class).invoke(target, args))

        Assert.assertEquals(target.awake(), AccessorBuilder.memberFunction("awake", TestObject::class).invoke(target))
        Assert.assertEquals(target.awake(), AccessorBuilder.memberFunction("doAwake", TestObject::class).invoke(target))
    }

    @Test
    fun testStaticFunction() {
        Assert.assertEquals(TestObject.initialize(), AccessorBuilder.staticFunction("initialize", TestObject::class).invoke())
    }
}
