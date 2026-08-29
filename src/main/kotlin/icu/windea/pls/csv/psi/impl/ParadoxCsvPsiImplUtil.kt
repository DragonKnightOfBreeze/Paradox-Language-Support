package icu.windea.pls.csv.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.psi.PsiPresentableElement
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.*
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.csv.text.ParadoxCsv
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.util.ParadoxExpressionManager
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
    fun getIdElement(element: ParadoxCsvColumn): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == COLUMN_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxCsvColumn, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Column
    }

    @JvmStatic
    fun getValue(element: ParadoxCsvColumn): String {
        return element.text.unquote(QuotePatterns.ParadoxCsv)
    }

    @JvmStatic
    fun setValue(element: ParadoxCsvColumn, value: String): ParadoxCsvColumn {
        return ParadoxCsvPsiManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxCsvColumn, content: String, range: TextRange): ParadoxCsvColumn {
        return ParadoxCsvPsiManipulationService.changeContent(element, content, range)
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getComponents(element: ParadoxCsvColumnContainer): List<ParadoxCsvColumn> {
        return element.findChildren<_>()
    }

    @JvmStatic
    fun getName(element: ParadoxCsvExpressionElement): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxCsvExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: ParadoxCsvExpressionElement, value: String): ParadoxCsvExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun setContent(element: ParadoxCsvExpressionElement, content: String, range: TextRange): ParadoxCsvExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getQuotePattern(element: PsiQuoteAwareElement): QuotePattern {
        return QuotePatterns.ParadoxCsv
    }

    @JvmStatic
    fun getPresentableText(element: PsiPresentableElement): String {
        return ParadoxCsvPsiService.getPresentableText(element)
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
        return ParadoxCsvPsiPresentation(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    // endregion
}
