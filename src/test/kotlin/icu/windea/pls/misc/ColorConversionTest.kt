package icu.windea.pls.misc

import com.intellij.ui.ColorUtil
import org.junit.Test
import java.awt.Color

class ColorConversionTest {
    @Test
    fun test() {
        val c3 = ColorUtil.toHex(Color(0, 0, 255, 127), true)
        val c4 = ColorUtil.toHex(Color.getHSBColor(0.6667f, 1f, 1f), true)
        println(c3)
        println(c4)
    }
}
