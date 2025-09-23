package icu.windea.pls.lang.psi.mock

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.core.castOrNull
import java.util.*

/**
 * 用于在某些地方绕过 IntelliJ Platform 的内部检查。
 */
class NavigationPsiElement(
    parent: NavigatablePsiElement
) : MockPsiElement(parent) {
    // 绕过以下位置的内部检查：
    // com.intellij.ide.impl.DataValidators.isPsiElementProvided

    override fun getTypeName(): String {
        return "Navigation Element"
    }

    override fun getNameIdentifier(): PsiElement? {
        return parent.castOrNull<PsiNameIdentifierOwner>()?.nameIdentifier ?: super.nameIdentifier
    }

    override fun getNavigationElement(): PsiElement {
        return parent
    }

    override fun getPresentation(): ItemPresentation? {
        return parent.castOrNull<NavigatablePsiElement>()?.presentation
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is NavigationPsiElement && parent == other.parent
    }

    override fun hashCode(): Int {
        return Objects.hash(parent)
    }
}
