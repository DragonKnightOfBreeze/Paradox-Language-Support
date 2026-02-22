package icu.windea.pls.csv.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiListLikeElement

/**
 * @see ParadoxCsvHeader
 * @see ParadoxCsvRow
 */
interface ParadoxCsvRowElement : NavigatablePsiElement, PsiListLikeElement {
    val columnList: List<ParadoxCsvColumn>

    override fun getComponents(): List<ParadoxCsvColumn> = columnList
}
