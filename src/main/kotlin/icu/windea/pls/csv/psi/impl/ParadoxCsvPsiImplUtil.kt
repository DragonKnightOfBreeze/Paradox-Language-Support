package icu.windea.pls.csv.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.cast
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.navigation.ParadoxCsvItemPresentation
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementFactory
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvPsiImplUtil {
    // region ParadoxCsvRowHeader

    @JvmStatic
    fun getIcon(element: ParadoxCsvHeader, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Row
    }

    // endregion

    // region ParadoxCsvRow

    @JvmStatic
    fun getIcon(element: ParadoxCsvRow, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Row
    }

    // endregion

    // region ParadoxCsvColumn

    @JvmStatic
    fun getIcon(element: ParadoxCsvColumn, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Column
    }

    @JvmStatic
    fun getValue(element: ParadoxCsvColumn): String {
        return element.text.unquote()
    }

    @JvmStatic
    fun setValue(element: ParadoxCsvColumn, value: String): ParadoxCsvColumn {
        val extraChars = ParadoxCsvPsiService.getSeparator().toString()
        val newValue = value.quoteIfNeeded(containAnyChar = extraChars, containBlank = false)
        val newElement = ParadoxCsvElementFactory.createColumnFromText(element.project, newValue)
        return element.replace(newElement).cast()
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getName(element: ParadoxCsvExpressionElement): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxCsvExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: ParadoxCsvExpressionElement, value: String): ParadoxScriptExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getExpression(element: ParadoxCsvExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun getComponents(element: PsiElement): List<PsiElement> {
        return element.findChildren { it is ParadoxCsvColumn }
    }

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun getReferences(element: ParadoxCsvExpressionElement): Array<out PsiReference> {
        return ParadoxExpressionManager.getReferences(element)
    }

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return ParadoxCsvItemPresentation(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    // endregion
}
