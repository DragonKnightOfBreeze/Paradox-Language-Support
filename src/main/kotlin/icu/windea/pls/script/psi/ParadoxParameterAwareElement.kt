package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * 名字中可以带有参数（[ParadoxParameter]）的 PSI 元素。
 *
 * 注意：实际上，脚本文件中的任何地方都能使用参数。
 */
interface ParadoxParameterAwareElement : PsiElement {
    val parameters: List<ParadoxParameter> get() = this.findChildren<_>()
}
