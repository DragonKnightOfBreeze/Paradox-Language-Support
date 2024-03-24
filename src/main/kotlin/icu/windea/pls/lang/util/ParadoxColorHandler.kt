package icu.windea.pls.lang.util

import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
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
        return ColorUtil.fromHex(hex)
    }
    
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
    
    fun getRgbColor(colorArgs: List<String>): Color? {
        val r = colorArgs.get(0).toInt().takeIf { it in 0..255 } ?: return null
        val g = colorArgs.get(1).toInt().takeIf { it in 0..255 } ?: return null
        val b = colorArgs.get(2).toInt().takeIf { it in 0..255 } ?: return null
        val a = colorArgs.getOrNull(3)?.toInt() ?: 255 // < 0 or > 255 is allowed
        val color = Color(r, g, b, a)
        return color
    }
    
    fun getHsvColor(colorArgs: List<String>): Color? {
        val h = colorArgs.get(0).toFloat().takeIf { it in 0f..1f } ?: return null
        val s = colorArgs.get(1).toFloat().takeIf { it in 0f..1f } ?: return null
        val v = colorArgs.get(2).toFloat().takeIf { it in 0f..1f } ?: return null
        val a = colorArgs.getOrNull(3)?.toFloat() ?: 1f // < 0.0 or > 1.0 is allowed
        val (r, g, b) = Color.getHSBColor(h, s, v)
        val color = Color(r, g, b, (a * 255f).toInt())
        return color
    }
    
    fun getColorType(element: PsiElement): String? {
        val configToGetOption = CwtConfigHandler.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
            .firstOrNull()
        if(configToGetOption == null) return null
        return getColorType(configToGetOption)
    }
    
    private fun getColorType(configToGetOption: CwtMemberConfig<*>): String? {
        return configToGetOption.findOption { it.key == "color_type" }?.stringValue
    }
}