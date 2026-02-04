package icu.windea.pls.core.util.properties

import org.junit.Assert
import org.junit.Test

class ObservableTest {
    @Suppress("ktPropBy")
    class Role(
        var name: String,
        val title: String,
        var tag: String
    ) {
        val displayName: String by ::name.observe { "$it, $title" }
        var tagSet: Set<String> by ::tag.fromCommandDelimitedString()
    }

    @Test
    fun test() {
        val obj = Role("Neuro", "Sama", "")
        val arg = "ai, vtuber, lovely, cute"
        val args = arrayOf("ai", "vtuber", "lovely", "cute")

        obj.name = "Evil Neuro"
        Assert.assertEquals("Evil Neuro, ${obj.title}", obj.displayName)

        obj.tag = arg
        Assert.assertEquals(setOf(arg), obj.tagSet)
        obj.tagSet = args.toSet()
        Assert.assertEquals(args.joinToString(","), obj.tag)
    }
}
