package icu.windea.pls.test.misc

import org.junit.*
import kotlin.reflect.full.*

class ReflectionTest {
    @Test(expected = UnsupportedOperationException::class)
    fun test1() {
        val javaClass = Class.forName("icu.windea.pls.ReflectionTestKt")
        val kotlinClass = javaClass.kotlin
        val testFunction = kotlinClass.functions.find { it.name == "test" }!!
        testFunction.call()
    }
}

private fun test() {
    println("123")
}
