package icu.windea.pls.csv.psi

import com.intellij.psi.util.*
import icu.windea.pls.core.*

fun ParadoxCsvColumn.isHeaderColumn(): Boolean {
    val parent = parent
    return parent is ParadoxCsvHeader
}

fun ParadoxCsvColumn.getColumnIndex(): Int {
    val rowElement = parent?.castOrNull<ParadoxCsvRowElement>() ?: return 0
    val index = rowElement.children().takeWhile { it != this }.count { it.elementType == ParadoxCsvElementTypes.SEPARATOR }
    return index
}

fun ParadoxCsvRowElement.getColumn(index: Int): ParadoxCsvColumn? {
    var indexRef = 0
    return this.children().takeWhile {
        if (it.elementType == ParadoxCsvElementTypes.SEPARATOR) {
            indexRef++
        }
        indexRef <= index
    }.lastOrNull { it is ParadoxCsvColumn }?.castOrNull()
}

fun ParadoxCsvColumn.getHeaderColumn(): ParadoxCsvColumn? {
    return parent?.castOrNull<ParadoxCsvRow>()?.getColumn(getColumnIndex())
}

fun ParadoxCsvColumn.getBodyColumn(): ParadoxCsvColumn? {
    return parent?.castOrNull<ParadoxCsvHeader>()?.getColumn(getColumnIndex())
}
