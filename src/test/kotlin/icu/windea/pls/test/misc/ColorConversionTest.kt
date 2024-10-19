package icu.windea.pls.test.misc

import com.intellij.ui.*
import org.junit.*
import java.awt.*

class ColorConversionTest {
    @Test
    fun test() {
        val c3 = ColorUtil.toHex(Color(0, 0, 255, 127), true)
        val c4 = ColorUtil.toHex(Color.getHSBColor(0.6667f, 1f, 1f), true)
        println(c3)
        println(c4)
    }
}
