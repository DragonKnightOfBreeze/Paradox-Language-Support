package icu.windea.pls.csv.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.cast
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.navigation.ParadoxCsvItemPresentation
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.util.ParadoxCsvManager
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvPsiImplUtil {
    //region ParadoxCsvRowHeader

    @JvmStatic
    fun getIcon(element: ParadoxCsvHeader, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Row
    }

    @JvmStatic
    fun toString(element: ParadoxCsvHeader): String {
        return "ParadoxCsvRowHeaderImpl(ROW_HEADER)"
    }

    //endregion

    //region ParadoxCsvRow

    @JvmStatic
    fun getIcon(element: ParadoxCsvRow, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Row
    }

    @JvmStatic
    fun toString(element: ParadoxCsvRow): String {
        return "ParadoxCsvRowImpl(ROW)"
    }

    //endregion

    //region ParadoxCsvColumn

    @JvmStatic
    fun getIcon(element: ParadoxCsvColumn, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.Column
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
        val extraChars = ParadoxCsvManager.getSeparator().toString()
        val newValue = value.quoteIfNecessary(extraChars = extraChars, blank = false)
        val newElement = ParadoxCsvElementFactory.createColumn(element.project, newValue)
        return element.replace(newElement).cast()
    }

    @JvmStatic
    fun toString(element: ParadoxCsvColumn): String {
        return "ParadoxCsvColumnImpl(COLUMN)"
    }

    //endregion

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return ParadoxCsvItemPresentation(element)
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
