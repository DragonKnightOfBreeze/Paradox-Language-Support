package icu.windea.pls.model.injection

import com.intellij.openapi.util.TextRange

/**
 * 本地化文本的语言注入信息。
 *
 * @property rangeInsideHost 原始文本在宿主 PSI 中的文本范围（不包含括起的双引号）。
 * @property localisationName 注入时使用的本地化的名字。
 */
class ParadoxLocalisationTextInjectionInfo(
    val rangeInsideHost: TextRange,
    val localisationName: String = "INJECTED",
)
