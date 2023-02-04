package icu.windea.pls.lang

import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.config.cwt.config.*
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
    
    @JvmStatic
    fun getColor(hex: String): Color {
        return ColorUtil.fromHex(hex)
    }
    
    @JvmStatic
    fun getColor(colorType: String, colorArgs: List<String>): Color? {
        when(colorType) {
            "rgb" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                val color = getRgbColor(colorArgs)
                return color
            }
            "hsv" -> {
                if(colorArgs.size != 3 && colorArgs.size != 4) throw IllegalStateException()
                val color = getHsvColor(colorArgs)
                return color
            }
            else -> {
                return null
            }
        }
    }
    
    @JvmStatic
    fun getRgbColor(colorArgs: List<String>): Color? {
        val r = colorArgs.get(0).toInt().takeIf { it in 0..255 } ?: return null
        val g = colorArgs.get(1).toInt().takeIf { it in 0..255 } ?: return null
        val b = colorArgs.get(2).toInt().takeIf { it in 0..255 } ?: return null
        val a = (colorArgs.getOrNull(3)?.toInt() ?: 255).takeIf { it in 0..255 } ?: return null
        val color = Color(r, g, b, a)
        return color
    }
    
    @JvmStatic
    fun getHsvColor(colorArgs: List<String>): Color? {
        val h = colorArgs.get(0).toDouble().takeIf { it in 0.0 .. 1.0 } ?: return null
        val s = colorArgs.get(1).toDouble().takeIf { it in 0.0 .. 1.0 } ?: return null
        val v = colorArgs.get(2).toDouble().takeIf { it in 0.0 .. 1.0 } ?: return null
        val a = (colorArgs.getOrNull(3)?.toDouble() ?: 1.0).takeIf { it in 0.0..1.0 } ?: return null
        val colorRgb = ColorConversions.convertHSVtoRGB(h * 360, s * 100, v * 100)
        val c = Color(colorRgb)
        val color = Color(c.red,  c.green, c.blue, (a * 255).toInt())
        return color
    }
    
    @JvmStatic
    fun getColorType(element: PsiElement): String? {
        val configToGetOption = ParadoxCwtConfigHandler.resolveConfigs(element, allowDefinitionSelf = true)
            .firstOrNull()
        if(configToGetOption == null) return null
        return getColorType(configToGetOption)
    }
    
    private fun getColorType(configToGetOption: CwtDataConfig<*>): String? {
        return configToGetOption.options?.find { it.key == "color_type" }?.stringValue
    }
}