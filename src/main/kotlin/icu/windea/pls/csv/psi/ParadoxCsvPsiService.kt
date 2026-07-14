package icu.windea.pls.csv.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.findChild
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.withDependencyItems

@Suppress("unused")
object ParadoxCsvPsiService {
    private val cachedColumnNamesKey = createKey<CachedValue<List<String>>>("cached.paradox.csv.columnNames")
    private const val SEPARATOR = ';'

    fun getSeparator(): Char {
        return SEPARATOR
    }

    fun getColumnNames(file: ParadoxCsvFile): List<String> {
        val header = file.header ?: return emptyList()
        return getColumnNamesFromCache(header)
    }

    fun getColumnNames(element: ParadoxCsvColumnContainer): List<String> {
        val header = element.castOrNull<ParadoxCsvHeader>() ?: element.containingFile?.castOrNull<ParadoxCsvFile>()?.header ?: return emptyList()
        return getColumnNamesFromCache(header)
    }

    private fun getColumnNamesFromCache(element: ParadoxCsvHeader): List<String> {
        return CachedValuesManager.getCachedValue(element, cachedColumnNamesKey) {
            val value = element.columnList.map { it.value }.optimized()
            value.withDependencyItems(element)
        }
    }

    fun getColumnSize(element: ParadoxCsvColumnContainer): Int {
        return element.children().count { it is ParadoxCsvColumn }
    }

    fun getColumn(element: ParadoxCsvColumnContainer, index: Int): ParadoxCsvColumn? {
        return element.children().filterIsInstance<ParadoxCsvColumn>().drop(index).firstOrNull()
    }

    fun isHeaderColumn(column: ParadoxCsvColumn): Boolean {
        return column.parent is ParadoxCsvHeader
    }

    fun isEmptyColumn(column: ParadoxCsvColumn): Boolean {
        return column.firstChild == null
    }

    fun getColumnIndex(column: ParadoxCsvColumn): Int {
        val rowElement = column.parent?.castOrNull<ParadoxCsvColumnContainer>() ?: return 0
        val index = rowElement.children().takeWhile { it != column }.count { it is ParadoxCsvColumn }
        return index
    }

    fun getHeaderColumn(column: ParadoxCsvColumn): ParadoxCsvColumn? {
        val header = column.parent?.castOrNull<ParadoxCsvRow>()?.parent?.findChild<ParadoxCsvHeader>() ?: return null
        val index = getColumnIndex(column)
        return getColumn(header, index)
    }

    fun getLocationForEmptyColumn(column: ParadoxCsvColumn): PsiElement {
        // special handle for empty columns
        val isFirst = getColumnIndex(column) == 0
        val result = column.siblings(forward = isFirst).find { it.elementType == ParadoxCsvElementTypes.SEPARATOR }
        return result ?: column
    }

    fun isLastRow(element: ParadoxCsvColumnContainer): Boolean {
        if (element !is ParadoxCsvRow) return false
        return element.siblings(withSelf = false).none { it is ParadoxCsvRow }
    }

    fun isLastColumn(element: ParadoxCsvColumn): Boolean {
        return element.siblings(withSelf = false).none { it is ParadoxCsvColumn }
    }
}
