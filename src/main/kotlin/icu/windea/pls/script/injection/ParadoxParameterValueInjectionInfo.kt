package icu.windea.pls.script.injection

import com.intellij.openapi.util.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement

/**
 * 参数值的语言注入信息。
 * @property text 用于语言注入的原始文本。
 * @property rangeInsideHost 原始文本在宿主PSI中的文本范围。
 * @property parameterElement 对应的参数PSI。必须使用懒加载。
 */
class ParadoxParameterValueInjectionInfo(
    val text: String,
    val rangeInsideHost: TextRange,
    val parameterValueQuoted: Boolean,
    parameterElementProvider: Lazy<ParadoxParameterElement?>
) {
    //这里必须使用懒加载，否则调用element.references时会导致SOF
    val parameterElement by parameterElementProvider
}
