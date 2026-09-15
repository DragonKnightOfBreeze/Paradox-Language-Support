package icu.windea.pls.csv.psi.impl

import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.children
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.select.listBy
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementManipulationService
import icu.windea.pls.csv.psi.ParadoxCsvElementPresentation
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.text.ParadoxCsv
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxFileManager
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object ParadoxCsvPsiImplUtil {
    // region ParadoxCsvFile

    @JvmStatic
    fun getHeader(element: ParadoxCsvFile): ParadoxCsvHeader? {
        return element.children().oneBy()
    }

    @JvmStatic
    fun getRows(element: ParadoxCsvFile): List<ParadoxCsvRow> {
        return element.children().listBy()
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxCsvFile, another: PsiElement?): Boolean {
        if (element === another) return true
        if (another !is ParadoxCsvFile) return false
        return ParadoxFileManager.isEquivalentFile(element, another)
    }

    // endregion

    // region ParadoxCsvHeader

    @JvmStatic
    fun getComponents(element: ParadoxCsvHeader): List<PsiElement> {
        return element.children().listBy<ParadoxCsvColumn>()
    }

    @JvmStatic
    fun getIcon(element: ParadoxCsvHeader, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Row
    }

    // endregion

    // region ParadoxCsvRow

    @JvmStatic
    fun getComponents(element: ParadoxCsvRow): List<PsiElement> {
        return element.children().listBy<ParadoxCsvColumn>()
    }

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
        return ParadoxCsvElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxCsvColumn, content: String, range: TextRange): ParadoxCsvColumn {
        return ParadoxCsvElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxCsvColumn): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    // endregion

    // region ParadoxCsvExpressionElement

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
    fun getPresentableText(element: ParadoxCsvExpressionElement): String {
        return element.value
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    @JvmStatic
    fun getPresentation(element: NavigatablePsiElement): ParadoxCsvElementPresentation {
        return ParadoxCsvElementPresentation(element)
    }

    @JvmStatic
    fun getQuotePattern(element: PsiQuoteAwareElement): QuotePattern {
        return QuotePatterns.ParadoxCsv
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

    // endregion
}
