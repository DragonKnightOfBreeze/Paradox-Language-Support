package icu.windea.pls.cwt.psi.impl

import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.constants.DefaultStrings
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.select.listBy
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtElementManipulationService
import icu.windea.pls.cwt.psi.CwtElementPresentation
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtPsiService
import icu.windea.pls.cwt.psi.CwtRootBlock
import icu.windea.pls.cwt.psi.CwtStatement
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.text.Cwt
import icu.windea.pls.model.constants.ChronicleStrings
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object CwtPsiImplUtil {
    // region CwtFile

    fun getBlock(element: CwtFile): CwtRootBlock? {
        return element.children().oneBy()
    }

    @JvmStatic
    fun getMemberContainer(element: CwtFile): CwtRootBlock? {
        return getBlock(element)
    }

    @JvmStatic
    fun getMembers(element: CwtFile): List<CwtMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    // endregion

    // region CwtRootBlock

    @JvmStatic
    fun getMemberContainer(element: CwtRootBlock): CwtRootBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: CwtRootBlock): List<CwtMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getComponents(element: CwtRootBlock): List<PsiElement> {
        return element.children().listBy<CwtStatement>()
    }

    // endregion

    // region CwtDocComment

    @JvmStatic
    fun getTokenType(element: CwtDocComment): IElementType {
        return DOC_COMMENT
    }

    @JvmStatic
    fun getOwner(element: CwtDocComment): PsiElement? {
        val attachingElement = PsiService.getAttachingElement(element) ?: return null
        if (!CwtPsiService.canAttachComment(attachingElement)) return null
        return attachingElement
    }

    // endregion

    // region CwtOptionComment

    @JvmStatic
    fun getTokenType(element: CwtOptionComment): IElementType {
        return OPTION_COMMENT
    }

    // endregion

    // region CwtOption

    @JvmStatic
    fun getIcon(element: CwtOption, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Option
    }

    @JvmStatic
    fun getName(element: CwtOption): String {
        return element.optionKey.value
    }

    @JvmStatic
    fun setName(element: CwtOption, name: String): CwtOption {
        throw IncorrectOperationException() // 不允许重命名
    }

    @JvmStatic
    fun getNameIdentifier(element: CwtOption): PsiElement {
        return element.optionKey
    }

    @JvmStatic
    fun getValue(element: CwtOption): String? {
        return element.optionValue?.value
    }

    @JvmStatic
    fun getPresentableText(element: CwtOption): String {
        var keyElement: CwtOptionKey? = null
        var valueElement: CwtValue? = null
        element.forEachChild { e ->
            when {
                e is CwtOptionKey -> keyElement = e
                e is CwtValue -> valueElement = e
            }
        }
        return buildString {
            if (keyElement != null) append(keyElement.presentableText) else append(DefaultStrings.unresolved)
            append(" = ")
            if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
        }
    }

    // endregion

    // region CwtOptionKey

    @JvmStatic
    fun getIcon(element: CwtOptionKey, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Option
    }

    @JvmStatic
    fun getName(element: CwtOptionKey): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: CwtOptionKey): String {
        return element.text.unquote(QuotePatterns.Cwt)
    }

    @JvmStatic
    fun getPresentableText(element: CwtOptionKey): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    @JvmStatic
    fun getQuotePattern(element: CwtOptionKey): QuotePattern {
        return QuotePatterns.Cwt
    }

    // endregion

    // region CwtProperty

    @JvmStatic
    fun getMemberContainer(element: CwtProperty): CwtBlock? {
        return element.propertyValue?.castOrNull<CwtBlock>()
    }

    @JvmStatic
    fun getMembers(element: CwtProperty): List<CwtMember>? {
        val memberContainer = getMemberContainer(element) ?: return null
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Property
    }

    @JvmStatic
    fun getName(element: CwtProperty): String {
        return element.propertyKey.value
    }

    @JvmStatic
    fun setName(element: CwtProperty, name: String): CwtProperty {
        throw IncorrectOperationException() // 不允许重命名
    }

    @JvmStatic
    fun getNameIdentifier(element: CwtProperty): PsiElement {
        return element.propertyKey
    }

    @JvmStatic
    fun getValue(element: CwtProperty): String? {
        return element.propertyValue?.value
    }

    @JvmStatic
    fun getPresentableText(element: CwtProperty): String {
        var keyElement: CwtPropertyKey? = null
        var separatorElement: PsiElement? = null
        var valueElement: CwtValue? = null
        element.forEachChild { e ->
            when {
                e is CwtPropertyKey -> keyElement = e
                CwtPsiService.isPropertySeparator(e) -> separatorElement = e
                e is CwtValue -> valueElement = e
            }
        }
        return buildString {
            if (keyElement != null) append(keyElement.presentableText) else append(DefaultStrings.unresolved)
            append(" ")
            append(separatorElement?.text ?: "=")
            append(" ")
            if (valueElement != null) append(valueElement.presentableText) else append(DefaultStrings.unresolved)
        }
    }

    // endregion

    // region CwtPropertyKey

    @JvmStatic
    fun getIcon(element: CwtPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Property
    }

    @JvmStatic
    fun getValue(element: CwtPropertyKey): String {
        return element.text.unquote(QuotePatterns.Cwt)
    }

    @JvmStatic
    fun setValue(element: CwtPropertyKey, value: String): CwtPropertyKey {
        return CwtElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: CwtPropertyKey, content: String, range: TextRange): CwtPropertyKey {
        return CwtElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: CwtPropertyKey): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    @JvmStatic
    fun getQuotePattern(element: CwtPropertyKey): QuotePattern {
        return QuotePatterns.Cwt
    }

    // endregion

    // region CwtValue

    @JvmStatic
    fun getIcon(element: CwtValue, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Value
    }

    @JvmStatic
    fun setValue(element: CwtValue, value: String): CwtValue {
        return CwtElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: CwtValue, content: String, range: TextRange): CwtValue {
        return CwtElementManipulationService.changeContent(element, content, range)
    }

    // endregion

    // region CwtString

    @JvmStatic
    fun getName(element: CwtString): String {
        return element.value
    }

    @JvmStatic
    fun setName(element: CwtString, name: String): CwtString {
        throw IncorrectOperationException() // 不允许重命名
    }

    @JvmStatic
    fun getNameIdentifier(element: CwtString): PsiElement {
        return element
    }

    @JvmStatic
    fun getValue(element: CwtString): String {
        return element.text.unquote(QuotePatterns.Cwt)
    }

    @JvmStatic
    fun setValue(element: CwtString, value: String): CwtString {
        return CwtElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: CwtString, content: String, range: TextRange): CwtString {
        return CwtElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: CwtString): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    // endregion

    // region CwtBlock

    @JvmStatic
    fun getMemberContainer(element: CwtBlock): CwtBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: CwtBlock): List<CwtMember> {
        val memberContainer = getMemberContainer(element)
        return memberContainer.children().listBy()
    }

    @JvmStatic
    fun getLeftBound(element: CwtBlock): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == LEFT_BRACE }
    }

    @JvmStatic
    fun getRightBound(element: CwtBlock): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == RIGHT_BRACE }
    }

    @JvmStatic
    fun getComponents(element: CwtBlock): List<PsiElement> {
        return element.children().listBy<CwtStatement>()
    }

    @JvmStatic
    fun getIcon(element: CwtBlock, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.Block
    }

    @JvmStatic
    fun getValue(element: CwtBlock): String {
        return ChronicleStrings.blockFolder
    }

    // endregion

    // region CwtExpressionElement

    @JvmStatic
    fun getName(element: CwtExpressionElement): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: CwtExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: CwtExpressionElement, value: String): CwtExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun setContent(element: CwtExpressionElement, content: String, range: TextRange): CwtExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getPresentableText(element: CwtExpressionElement): String {
        return element.value
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return GlobalSearchScope.allScope(element.project)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    @JvmStatic
    fun getPresentation(element: NavigatablePsiElement): CwtElementPresentation {
        return CwtElementPresentation(element)
    }

    @JvmStatic
    fun getQuotePattern(element: PsiQuoteAwareElement): QuotePattern {
        return QuotePatterns.Cwt
    }

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    // endregion
}
