package icu.windea.pls.core

import icu.windea.pls.core.accessor.AccessorObject
import org.junit.Assert
import org.junit.Test

class AccessorExtensionsTest {
    private val obj = AccessorObject("Windea", "Female", "Utopian Sapient", 24)
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
        Assert.assertEquals(AccessorObject.initializedStatus, staticProperty<AccessorObject, Boolean>("initializedStatus").get())
        Assert.assertEquals(AccessorObject.initializedStatus, staticProperty<Boolean>("initializedStatus", targetClassName).get())
    }

    @Test
    fun function_smokeTest() {
        Assert.assertEquals(obj.helloWorld(), obj.function("helloWorld").call())
        Assert.assertEquals(obj.hello("my dragon"), function(obj, "hello", targetClassName).call("my dragon"))
    }

    @Test
    fun memberFunction_smokeTest() {
        Assert.assertEquals(obj.helloWorld(), memberFunction<AccessorObject>("helloWorld").call(obj))
        Assert.assertEquals(obj.hello("my dragon"), memberFunction("hello", targetClassName).call(obj, "my dragon"))
    }

    @Test
    fun staticFunction_smokeTest() {
        Assert.assertEquals(AccessorObject.initialize(), staticFunction<AccessorObject>("initialize").call())
        Assert.assertEquals(AccessorObject.initialize(), staticFunction("initialize", targetClassName).call())
    }
}
