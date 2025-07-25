package icu.windea.pls.cwt.psi

import com.intellij.psi.*

/**
 * @see CwtPropertyKey
 * @see CwtString
 */
interface CwtStringExpressionElement : CwtExpressionElement, PsiLiteralValue, ContributedReferenceHost {
    override fun getName(): String

    override val value: String
}
