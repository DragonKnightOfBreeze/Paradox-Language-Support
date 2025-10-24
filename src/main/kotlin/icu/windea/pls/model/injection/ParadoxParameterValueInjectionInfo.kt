package icu.windea.pls.model.injection

import com.intellij.openapi.util.TextRange
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

/**
 * 参数值的语言注入信息。
 *
 * @property rangeInsideHost 原始文本在宿主 PSI 中的文本范围。
 * @property parameterValueQuoted 参数值是否用引号括起。
 * @property parameterElement 对应的参数 PSI（懒加载）。
 */
class ParadoxParameterValueInjectionInfo(
    val rangeInsideHost: TextRange,
    val parameterValueQuoted: Boolean,
    parameterElementProvider: Lazy<ParadoxParameterElement?>
) {
    // 这里必须使用懒加载，否则调用 `element.references` 时会导致 SOF
    val parameterElement by parameterElementProvider
}
