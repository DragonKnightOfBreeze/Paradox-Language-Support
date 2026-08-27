package icu.windea.pls.cwt.psi

import icu.windea.pls.core.psi.PsiPresentableElement

/**
 * 成员。包括属性和值。
 *
 * @see CwtProperty
 * @see CwtValue
 */
interface CwtMember : CwtStatement, CwtMemberContext, PsiPresentableElement
