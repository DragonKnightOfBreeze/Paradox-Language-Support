package icu.windea.pls.model.injection

import com.intellij.openapi.util.TextRange

/**
 * 本地化文本的语言注入信息。
 *
 * @property rangeInsideHost 原始文本在宿主 PSI 中的文本范围（包含且要求括起的双引号）。
 */
class ParadoxLocalisationTextInjectionInfo(
    val rangeInsideHost: TextRange
)
