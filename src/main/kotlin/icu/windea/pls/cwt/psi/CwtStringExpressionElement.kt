package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.PsiQuoteAwareElement

/**
 * 可以作为字符串表达式的 [PsiElement]。
 *
 * @see CwtPropertyKey
 * @see CwtString
 */
interface CwtStringExpressionElement : CwtExpressionElement, CwtLiteralValue, PsiQuoteAwareElement
