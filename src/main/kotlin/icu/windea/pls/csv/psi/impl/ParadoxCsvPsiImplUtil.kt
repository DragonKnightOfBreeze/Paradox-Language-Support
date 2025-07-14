package icu.windea.pls.csv.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvPsiImplUtil {
    //region ParadoxCsvRowHeader

    @JvmStatic
    fun getIcon(element: ParadoxCsvHeader, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CsvRow
    }

    @JvmStatic
    fun getComponents(element: ParadoxCsvHeader): List<ParadoxCsvColumn> {
        return element.findChildren<_>()
    }

    @JvmStatic
    fun toString(element: ParadoxCsvHeader): String {
        return "ParadoxCsvRowHeaderImpl(ROW_HEADER)"
    }

    //endregion

    //region ParadoxCsvRow

    @JvmStatic
    fun getIcon(element: ParadoxCsvRow, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CsvRow
    }

    @JvmStatic
    fun getComponents(element: ParadoxCsvRow): List<ParadoxCsvColumn> {
        return element.findChildren<_>()
    }

    @JvmStatic
    fun toString(element: ParadoxCsvRow): String {
        return "ParadoxCsvRowImpl(ROW)"
    }

    //endregion

    //region ParadoxCsvColumn

    @JvmStatic
    fun getIcon(element: ParadoxCsvColumn, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CsvColumn
    }

    @JvmStatic
    fun getName(element: ParadoxCsvColumn): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxCsvColumn): String {
        return element.text.unquote()
    }

    @JvmStatic
    fun setValue(element: ParadoxCsvColumn, value: String): ParadoxCsvColumn {
        val finalValue = if (value.contains(ParadoxCsvManager.getSeparator())) value.quote() else value
        val newElement = ParadoxCsvElementFactory.createColumn(element.project, finalValue)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun toString(element: ParadoxCsvColumn): String {
        return "ParadoxCsvColumnImpl(COLUMN)"
    }

    //endregion

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return BaseParadoxItemPresentation(element)
    }

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }
}
