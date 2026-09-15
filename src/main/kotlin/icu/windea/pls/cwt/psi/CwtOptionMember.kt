package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 选项成员。包括选项和值。
 *
 * @see CwtOption
 * @see CwtValue
 */
interface CwtOptionMember : NavigatablePsiElement, PsiPresentableTextAwareElement
