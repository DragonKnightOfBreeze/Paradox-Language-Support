package icu.windea.pls.script.psi

import com.intellij.psi.PsiLiteralValue

/**
 * 作为字面量值（布尔值/数字/字符串）的 PSI 元素。
 *
 * @see ParadoxScriptBoolean
 * @see ParadoxScriptNumberExpressionElement
 * @see ParadoxScriptStringExpressionElement
 * @see ParadoxScriptInlineMathNumber
 */
interface ParadoxScriptLiteralValue : PsiLiteralValue
