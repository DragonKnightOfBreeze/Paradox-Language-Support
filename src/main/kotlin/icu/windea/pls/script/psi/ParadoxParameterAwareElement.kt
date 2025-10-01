package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 表示此 PSI 元素可以带有参数（[ParadoxParameter]）。
 *
 * 注意：实际上，脚本文件中的任何地方都能使用参数。
 *
 * @see ParadoxParameter
 */
interface ParadoxParameterAwareElement : PsiElement {
    val parameters: List<ParadoxParameter> get() = this.findChildren<_>()
}
