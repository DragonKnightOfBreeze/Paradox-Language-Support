package icu.windea.pls.cwt.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 语句。目前仅包括成员。
 *
 * @see CwtMember
 */
interface CwtStatement : NavigatablePsiElement, PsiPresentableTextAwareElement
