package icu.windea.pls.csv.psi

import com.intellij.psi.PsiElement

/**
 * 列容器。可以直接包含列（[ParadoxCsvColumn]）。
 *
 * @see ParadoxCsvHeader
 * @see ParadoxCsvRow
 */
interface ParadoxCsvColumnContainer : PsiElement {
    val columnList: List<ParadoxCsvColumn> get() = emptyList()
}
