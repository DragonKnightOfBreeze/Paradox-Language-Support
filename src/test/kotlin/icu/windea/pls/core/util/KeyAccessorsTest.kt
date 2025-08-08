package icu.windea.pls.core.util

import com.intellij.openapi.util.*
import org.junit.Assert
import org.junit.Test

class KeyAccessorsTest {
    class Obj : UserDataHolderBase()

    val obj = Obj()
    val k1: Key<String> = createKey("k1")
    val k2: Key<String> = createKey("k2")

    @Test
    fun testGetOrPutUserData() {
        var count = 0
        Assert.assertEquals(null, obj.getUserData(k1))
        Assert.assertEquals("value", obj.getOrPutUserData(k1) { count++; "value" })
        Assert.assertEquals(1, count)
        Assert.assertEquals(null, obj.getUserData(k2))
        Assert.assertEquals(null, obj.getOrPutUserData(k2) { count++; null })
        Assert.assertEquals(2, count)
        Assert.assertEquals(null, obj.getOrPutUserData(k2) { count++; null })
        Assert.assertEquals(2, count)
        Assert.assertEquals(null, obj.getOrPutUserData(k2) { count++; "value" })
        Assert.assertEquals(2, count)
    }
}
