package icu.windea.pls.lang.codeInsight.color

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.allFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.ui.ColorService
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.isValidExpression
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptNumberExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.containingDirectMember
import java.awt.Color

@Optimized
object ParadoxColorFactory {
    /**
     * 得到当前 [element] 对应的颜色类型。
     *
     * 说明：
     * - 如果是 [ParadoxScriptColor]，则直接从语法获取。否则从规则获取（匹配的规则的对应的选项 `## color_type` 的值）。
     * - 可选值包括 `hex` `rgb` `hsv` `hsv360`。
     * - 其中，`hex` 仅适用于 [ParadoxScriptString]，其他可选值适用于 [ParadoxScriptBlock] 和 [ParadoxScriptColor]。
     */
    fun getColorType(element: ParadoxScriptValue): String? {
        // from syntax
        if (element is ParadoxScriptColor) return element.colorType

        // from configs
        if (element !is ParadoxScriptString && element !is ParadoxScriptBlock) return null
        val memberElement = element.containingDirectMember
        val options = ParadoxMatchOptions(forDeclarationRoot = true)
        val config = ParadoxConfigManager.getConfigs(memberElement, options).firstOrNull() ?: return null
        return config.optionMetadata.colorType
    }

    /**
     * 得到当前 [element] 对应的颜色参数。
     *
     * 说明：
     * - 要求是以 `0x` 开始的，6位或8位的十六进制整数字符串，如 `0xff0000`。
     */
    fun getColorArg(element: ParadoxScriptString): String? {
        return element.value.orNull()
            ?.takeIf { it.startsWith("0x", ignoreCase = true) }
            ?.takeIf { it.length == 8 || it.length == 10 }
    }

    /**
     * 得到当前 [element] 对应的颜色参数。
     *
     * 说明：
     * - 如果是 [ParadoxScriptColor]，则直接从语法获取。如果是 [ParadoxScriptBlock]，则尝试将其视为数字数组，从数组元素获取。
     */
    fun getColorArgs(element: ParadoxScriptValue): List<String>? {
        if (element is ParadoxScriptColor) return element.colorArgs

        if (element !is ParadoxScriptBlock) return null
        return element.valueList.orNull()
            ?.takeIf { it.size == 3 || it.size == 4 && it.allFast { v -> v.isValidExpression() } }
            ?.mapFast { it.resolved() ?: return null }
            ?.takeIf { it.allFast { v -> v is ParadoxScriptNumberExpressionElement } }
            ?.mapFast { it.value }
    }

    fun getColor(colorArg: String): Color? {
        return ColorService.getColorFromHex(colorArg)
    }

    fun getColor(colorType: String, colorArgs: List<String>): Color? {
        return when (colorType) {
            "rgb" -> ColorService.getColorFromRgb(colorArgs)
            "hsv" -> ColorService.getColorFromHsv(colorArgs)
            "hsv360" -> ColorService.getColorFromHsv360(colorArgs)
            else -> null
        }
    }

    fun getNewColorArg(colorArg: String, newColor: Color): String? {
        return ColorService.getNewColorArgFromHex(colorArg, newColor)
    }

    fun getNewColorArgs(colorType: String, colorArgs: List<String>, newColor: Color, precision: Int = -3): List<String>? {
        // 默认保留3位小数
        return when (colorType) {
            "rgb" -> ColorService.getNewColorArgsFromRgb(colorArgs, newColor, precision)
            "hsv" -> ColorService.getNewColorArgsFromHsv(colorArgs, newColor, precision)
            "hsv360" -> ColorService.getNewColorArgsFromHsv360(colorArgs, newColor)
            else -> null
        }
    }
}
