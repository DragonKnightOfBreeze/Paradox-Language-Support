package icu.windea.pls.core

import icu.windea.pls.core.accessor.AccessorObject
import org.junit.Assert
import org.junit.Test

class AccessorExtensionsTest {
    private val obj = AccessorObject("Windea", "Female", 10000)
    private val targetClassName = AccessorObject::class.java.name

    @Test
    fun property_smokeTest() {
        Assert.assertEquals(obj.name, obj.property<_, String>("name").get())
        Assert.assertEquals(obj.name, obj.property<String>("name", targetClassName).get())
    }

    @Test
    fun memberProperty_smokeTest() {
        Assert.assertEquals(obj.name, memberProperty<AccessorObject, String>("name").get(obj))
        Assert.assertEquals(obj.name, memberProperty<String>("name", targetClassName).get(obj))
    }

    @Test
    fun staticProperty_smokeTest() {
        Assert.assertEquals(AccessorObject.information, staticProperty<AccessorObject, String>("information").get())
        Assert.assertEquals(AccessorObject.information, staticProperty<String>("information", targetClassName).get())
    }

    @Test
    fun function_smokeTest() {
        Assert.assertEquals(obj.helloWorld(), obj.function("helloWorld").call())
        Assert.assertEquals(obj.hello("the world"), function(obj, "hello", targetClassName).call("the world"))
    }

    @Test
    fun memberFunction_smokeTest() {
        Assert.assertEquals(obj.helloWorld(), memberFunction<AccessorObject>("helloWorld").call(obj))
        Assert.assertEquals(obj.hello("the world"), memberFunction("hello", targetClassName).call(obj, "the world"))
    }

    @Test
    fun staticFunction_smokeTest() {
        Assert.assertEquals(AccessorObject.greetings(), staticFunction<AccessorObject>("greetings").call())
        Assert.assertEquals(AccessorObject.greetings(), staticFunction("greetings", targetClassName).call())
    }
}
