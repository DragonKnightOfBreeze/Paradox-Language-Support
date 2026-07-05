package icu.windea.pls.csv.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiListLikeElement

/**
 * 列容器。可以直接包含 [ParadoxCsvColumn]。
 *
 * @see ParadoxCsvHeader
 * @see ParadoxCsvRow
 */
interface ParadoxCsvColumnContainer : NavigatablePsiElement, PsiListLikeElement {
    val columnList: List<ParadoxCsvColumn>
}
