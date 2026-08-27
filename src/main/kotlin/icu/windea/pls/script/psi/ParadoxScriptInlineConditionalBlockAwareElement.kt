package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.findChildren

/**
 * 名字中可以包含内联条件化块（[ParadoxScriptInlineConditionalBlock]）的 PSI 元素。
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 * @see ParadoxScriptScriptedVariableName
 * @see ParadoxScriptScriptedVariableReference
 * @see ParadoxScriptInlineMathScriptedVariableReference
 */
@Suppress("unused")
interface ParadoxScriptInlineConditionalBlockAwareElement : PsiElement {
    val inlineConditionalBlocks: List<ParadoxScriptInlineConditionalBlock> get() = this.findChildren<_>()
}
