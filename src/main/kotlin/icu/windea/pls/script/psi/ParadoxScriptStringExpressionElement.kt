package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * 可以作为字符串表达式的 [PsiElement]。
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, ParadoxScriptLiteralValue
