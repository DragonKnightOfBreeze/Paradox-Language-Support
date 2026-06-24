@file:Suppress("unused")

package icu.windea.pls.csv.psi

fun ParadoxCsvRowElement.getColumnSize(): Int = ParadoxCsvPsiService.getColumnSize(this)

fun ParadoxCsvRowElement.getColumn(index: Int): ParadoxCsvColumn? = ParadoxCsvPsiService.getColumn(this, index)

fun ParadoxCsvColumn.isEmptyColumn(): Boolean = ParadoxCsvPsiService.isEmptyColumn(this)

fun ParadoxCsvColumn.isHeaderColumn(): Boolean = ParadoxCsvPsiService.isHeaderColumn(this)

fun ParadoxCsvColumn.getColumnIndex(): Int = ParadoxCsvPsiService.getColumnIndex(this)

fun ParadoxCsvColumn.getHeaderColumn(): ParadoxCsvColumn? = ParadoxCsvPsiService.getHeaderColumn(this)
