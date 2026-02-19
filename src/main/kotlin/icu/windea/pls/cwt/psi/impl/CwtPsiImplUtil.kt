package icu.windea.pls.cwt.psi.impl

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.cast
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findChild
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.navigation.CwtItemPresentation
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtElementFactory
import icu.windea.pls.cwt.psi.CwtElementTypes.*
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtPsiUtil
import icu.windea.pls.cwt.psi.CwtRootBlock
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.constants.PlsStrings
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object CwtPsiImplUtil {
    // region CwtRootBlock

    @JvmStatic
    fun getValue(element: CwtRootBlock): String {
        return PlsStrings.blockFolder
    }

    @JvmStatic
    fun getMembersRoot(element: CwtRootBlock): CwtRootBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: CwtRootBlock): List<CwtMember> {
        return getMembersRoot(element).findChildren<_>()
    }

    // endregion

    // region CwtOption

    @JvmStatic
    fun getIcon(element: CwtOption, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtOption
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
    fun getSeparatorType(element: CwtOption): CwtSeparatorType {
        // 这里不能遍历element.children
        element.forEachChild { child ->
            when (child.elementType) {
                EQUAL_SIGN -> return CwtSeparatorType.EQUAL
                NOT_EQUAL_SIGN -> return CwtSeparatorType.NOT_EQUAL
            }
        }
        return CwtSeparatorType.EQUAL
    }

    // endregion

    // region CwtOptionKey

    @JvmStatic
    fun getIcon(element: CwtOptionKey, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtOption
    }

    @JvmStatic
    fun getName(element: CwtOptionKey): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: CwtOptionKey): String {
        return element.findChild { it.elementType == OPTION_KEY_TOKEN }!!.text.unquote()
    }

    // endregion

    // region CwtProperty

    @JvmStatic
    fun getIcon(element: CwtProperty, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtProperty
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
    fun getSeparatorType(element: CwtProperty): CwtSeparatorType {
        // 这里不能遍历element.children
        element.forEachChild { child ->
            when (child.elementType) {
                EQUAL_SIGN -> return CwtSeparatorType.EQUAL
                NOT_EQUAL_SIGN -> return CwtSeparatorType.NOT_EQUAL
            }
        }
        return CwtSeparatorType.EQUAL
    }

    @JvmStatic
    fun getMembersRoot(element: CwtProperty): CwtBlock? {
        return element.propertyValue?.castOrNull<CwtBlock>()
    }

    @JvmStatic
    fun getMembers(element: CwtProperty): List<CwtMember>? {
        return getMembersRoot(element)?.findChildren<_>()
    }

    // endregion

    // region CwtPropertyKey

    @JvmStatic
    fun getIcon(element: CwtPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtProperty
    }

    @JvmStatic
    fun getName(element: CwtPropertyKey): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: CwtPropertyKey): String {
        return element.findChild { it.elementType == PROPERTY_KEY_TOKEN }!!.text.unquote()
    }

    @JvmStatic
    fun setValue(element: CwtPropertyKey, value: String): CwtPropertyKey {
        val newValue = value.quoteIfNecessary()
        val newElement = CwtElementFactory.createPropertyKey(element.project, newValue)
        return element.replace(newElement).cast()
    }

    // endregion

    // region CwtValue

    @JvmStatic
    fun getIcon(element: CwtValue, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtValue
    }

    @JvmStatic
    fun getName(element: CwtValue): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: CwtValue): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: CwtValue, value: String): CwtValue {
        if (element is CwtString) return setValue(element, value)
        val newElement = CwtElementFactory.createValue(element.project, value)
        return element.replace(newElement).cast()
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
        return element.text.unquote()
    }

    @JvmStatic
    fun setValue(element: CwtString, value: String): CwtString {
        val newValue = value.quoteIfNecessary()
        val newElement = CwtElementFactory.createString(element.project, newValue)
        return element.replace(newElement).cast()
    }

    // endregion

    // region CwtBlock

    @JvmStatic
    fun getIcon(element: CwtBlock, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.CwtBlock
    }

    @JvmStatic
    fun getName(element: CwtBlock): String {
        return element.value
    }

    @JvmStatic
    fun setName(element: CwtBlock, name: String): CwtBlock {
        throw IncorrectOperationException() // 不允许重命名
    }

    @JvmStatic
    fun getValue(element: CwtBlock): String {
        return PlsStrings.blockFolder
    }

    @JvmStatic
    fun getMembersRoot(element: CwtBlock): CwtBlock {
        return element
    }

    @JvmStatic
    fun getMembers(element: CwtBlock): List<CwtMember> {
        return getMembersRoot(element).findChildren<_>()
    }

    @JvmStatic
    fun getLeftBound(element: CwtBlock): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == LEFT_BRACE }
    }

    @JvmStatic
    fun getRightBound(element: CwtBlock): PsiElement? {
        return element.lastChild?.takeIf { it.elementType == RIGHT_BRACE }
    }

    // endregion

    // region CwtDocComment

    @JvmStatic
    fun getTokenType(element: CwtDocComment): IElementType {
        return DOC_COMMENT
    }

    @JvmStatic
    fun getOwner(element: CwtDocComment): PsiElement? {
        val attachingElement = PlsPsiManager.getAttachingElement(element) ?: return null
        if (!CwtPsiUtil.canAttachComment(attachingElement)) return null
        return attachingElement
    }

    // endregion

    // region CwtOptionComment

    @JvmStatic
    fun getTokenType(element: CwtOptionComment): IElementType {
        return OPTION_COMMENT
    }

    // endregion

    @JvmStatic
    fun getComponents(element: PsiElement): List<PsiElement> {
        return element.findChildren { isComponent(it) }
    }

    private fun isComponent(element: PsiElement): Boolean {
        return element is CwtMember
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
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return GlobalSearchScope.allScope(element.project)
    }

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return CwtItemPresentation(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PlsPsiManager.toPresentableString(element)
    }
}
