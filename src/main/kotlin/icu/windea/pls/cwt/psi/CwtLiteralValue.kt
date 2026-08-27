package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiLiteralValue

/**
 * 作为字面量值（布尔值/数字/字符串）的 PSI 元素。
 *
 * @see CwtBoolean
 * @see CwtNumberExpressionElement
 * @see CwtStringExpressionElement
 */
interface CwtLiteralValue : PsiLiteralValue
