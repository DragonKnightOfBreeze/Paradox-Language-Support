package icu.windea.pls.csv.psi

import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.findChild

object ParadoxCsvPsiService {
    private const val SEPARATOR = ';'

    fun getSeparator(): Char {
        return SEPARATOR
    }

    fun isEmptyColumn(column: ParadoxCsvColumn): Boolean {
        return column.firstChild == null
    }

    fun isHeaderColumn(column: ParadoxCsvColumn): Boolean {
        return column.parent is ParadoxCsvHeader
    }

    fun getColumn(element: ParadoxCsvRowElement, index: Int): ParadoxCsvColumn? {
        return element.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
    }

    fun getHeaderColumn(column: ParadoxCsvColumn): ParadoxCsvColumn? {
        val header = column.parent?.castOrNull<ParadoxCsvRow>()?.parent?.findChild<ParadoxCsvHeader>() ?: return null
        val index = getColumnIndex(column)
        return getColumn(header, index)
    }

    fun getColumnSize(element: ParadoxCsvRowElement): Int {
        return element.children().count { it is ParadoxCsvColumn }
    }

    fun getColumnIndex(column: ParadoxCsvColumn): Int {
        val rowElement = column.parent?.castOrNull<ParadoxCsvRowElement>() ?: return 0
        val index = rowElement.children().takeWhile { it != column }.count { it is ParadoxCsvColumn }
        return index
    }
}
