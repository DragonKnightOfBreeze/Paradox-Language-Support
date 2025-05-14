package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.findChildren

/**
 * 表示此PSI可以带有参数（[ParadoxParameter]）。
 *
 * 注意：实际上，脚本文件中的任何地方都能使用参数。
 *
 * @see ParadoxParameter
 */
interface ParadoxParameterAwareElement : PsiElement {
    val parameters: List<ParadoxParameter> get() = this.findChildren<_>()
}
