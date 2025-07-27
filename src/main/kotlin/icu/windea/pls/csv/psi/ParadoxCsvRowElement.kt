package icu.windea.pls.csv.psi

import com.intellij.psi.*
import icu.windea.pls.core.*

/**
 * @see ParadoxCsvHeader
 * @see ParadoxCsvRow
 */
interface ParadoxCsvRowElement : NavigatablePsiElement, PsiListLikeElement {
    override fun getComponents(): List<ParadoxCsvColumn> {
        return findChildren<_>()
    }
}
