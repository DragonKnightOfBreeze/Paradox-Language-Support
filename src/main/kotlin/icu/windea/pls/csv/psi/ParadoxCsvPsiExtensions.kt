package icu.windea.pls.csv.psi

import icu.windea.pls.core.*

fun ParadoxCsvRowElement.getColumnSize(): Int {
    return this.children().count { it is ParadoxCsvColumn }
}

fun ParadoxCsvRowElement.getColumn(index: Int): ParadoxCsvColumn? {
    return this.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
}

fun ParadoxCsvColumn.isEmptyColumn(): Boolean {
    return firstChild == null
}

fun ParadoxCsvColumn.isHeaderColumn(): Boolean {
    return parent is ParadoxCsvHeader
}

fun ParadoxCsvColumn.getColumnIndex(): Int {
    val rowElement = parent?.castOrNull<ParadoxCsvRowElement>() ?: return 0
    val index = rowElement.children().takeWhile { it != this }.count { it is ParadoxCsvColumn }
    return index
}

fun ParadoxCsvColumn.getHeaderColumn(): ParadoxCsvColumn? {
    val header = parent?.castOrNull<ParadoxCsvRow>()?.parent?.findChild<ParadoxCsvHeader>() ?: return null
    val index = getColumnIndex()
    return header.getColumn(index)
}
