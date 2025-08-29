package icu.windea.pls.core.util.properties

import org.junit.Assert
import org.junit.Test

class ObservableTest {
    @Test
    fun test() {
        val obj = ObservableObject("Windea", "the Dragon Knight", "")
        val arg = "the seeker"
        val args = arrayOf("the seeker", "the ranger", "and all my pals")

        obj.name = "Windea - Future Seeker"
        Assert.assertEquals("Windea - Future Seeker, ${obj.suffix}", obj.displayName)

        obj.pals = arg
        Assert.assertEquals(setOf(arg), obj.palSet)
        obj.palSet = args.toSet()
        Assert.assertEquals(args.joinToString(","), obj.pals)
    }
}
