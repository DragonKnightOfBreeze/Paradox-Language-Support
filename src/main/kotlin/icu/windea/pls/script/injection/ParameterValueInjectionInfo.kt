package icu.windea.pls.script.injection

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

/**
 * 参数值的语言注入信息。
 * @property text 用于语言注入的原始文本。
 * @property rangeInsideHost 原始文本在宿主PSI中的文本范围。
 * @property parameterElement 对应的参数PSI。必须使用懒加载。
 * @property textFragments 经由需要特殊处理的字符分割后得到的文本碎片信息，用于进行实际的语言注入。
 */
class ParameterValueInjectionInfo(
    val text: String,
    val rangeInsideHost: TextRange,
    val parameterValueQuoted: Boolean,
    parameterElementProvider: Lazy<ParadoxParameterElement?>
) {
    //这里必须使用懒加载，否则调用element.references时会导致SOF
    val parameterElement by parameterElementProvider
    
    val textFragments = text.getTextFragments(rangeInsideHost.startOffset)
}
