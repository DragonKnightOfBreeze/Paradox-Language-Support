package icu.windea.pls.script.psi

import com.intellij.psi.PsiLiteralValue

/**
 * 字面量值。包括布尔值、数字、字符串。
 *
 * @see ParadoxScriptBoolean
 * @see ParadoxScriptNumberExpressionElement
 * @see ParadoxScriptStringExpressionElement
 * @see ParadoxScriptInlineMathNumber
 */
interface ParadoxScriptLiteralValue : PsiLiteralValue
