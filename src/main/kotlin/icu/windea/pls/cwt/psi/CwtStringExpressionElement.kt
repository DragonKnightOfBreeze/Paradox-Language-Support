package icu.windea.pls.cwt.psi

/**
 * @see CwtPropertyKey
 * @see CwtString
 */
interface CwtStringExpressionElement: CwtExpressionElement {
    override fun getName(): String
    
    override val value: String
}
