package icu.windea.pls.misc

import icu.windea.pls.core.*
import org.junit.*
import kotlin.reflect.full.*

class ReflectionTest {
    @Test(expected = Exception::class)
    fun test1() {
        val javaClass = Class.forName("icu.windea.pls.ReflectionTestKt")
        val kotlinClass = javaClass.kotlin
        val testFunction = kotlinClass.functions.find { it.name == "test" }!!
        testFunction.call()
    }

    @Test
    fun testModifyFinalField1() {
        val foo = Foo("a")
        Assert.assertEquals("a", foo.name)
        val nameProp = foo.property<_, String>("name")
        nameProp.set("b")
        Assert.assertEquals("b", foo.name)
    }

    @Test
    fun testModifyFinalField2() {
        val foo = Foo("a")
        Assert.assertEquals("a", foo.name)
        val memberProp = memberProperty<Foo, String>("name")
        memberProp.set(foo, "b")
        Assert.assertEquals("b", foo.name)
    }
}

private fun test() {
    println("123")
}

private data class Foo(
    val name: String
)
