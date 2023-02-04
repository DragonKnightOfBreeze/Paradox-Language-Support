package icu.windea.pls.lang

import org.apache.commons.imaging.color.*
import java.awt.*

object ParadoxColorHandler {
    // 0xffffffff (ignore case, 8 or 10 length)
    
    // rgb { $r $g $b }
    // rgb { $r $g $b $a }
    // r,g,b,a - int[0..255]
    
    // hsv { $h $s $v }
    // hsv { $h $s $v $a }
    // h,s,v,a - int[0..255] or float[0.0..1.0]
    
    fun getColor(hex: String): Color {
        return Color(hex.toInt())
    }
    
    fun getColor(colorType: String, colorArgs: List<String>): Color? {
        when(colorType) {
            "rgb" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                val color = colorArgs.map { it.toInt() }.let { Color(it[0], it[1], it[2], it.getOrNull(3) ?: 255) }
                return color
            }
            "hsv" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                //args.takeIf { it.size == 3 }?.map { it.toFloat() }?.let {  }
                val colorRgb = colorArgs.map { it.toDouble() }.let { ColorConversions.convertHSVtoRGB(it[0], it[1], it[2]) }
                val colorRgba = (colorRgb shl 8) + ((colorArgs.getOrNull(3)?.toFloatOrNull() ?: 1.0f) * 255).toInt()
                val color = Color(colorRgba)
                return color
            }
            else -> {
                return null
            }
        }
    }
}