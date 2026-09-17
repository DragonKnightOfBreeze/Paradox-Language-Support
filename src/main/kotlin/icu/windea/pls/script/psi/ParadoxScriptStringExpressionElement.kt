package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiQuoteAwareElement

/**
 * 可以作为字符串表达式的 [PsiElement]。
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, ParadoxScriptLiteralValue, PsiQuoteAwareElement
