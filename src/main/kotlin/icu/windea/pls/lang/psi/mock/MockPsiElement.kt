package icu.windea.pls.lang.psi.mock

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.RenameableFakePsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import icu.windea.pls.lang.search.scope.ParadoxSearchScope

@Suppress("UnstableApiUsage")
abstract class MockPsiElement(parent: PsiElement) : RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun getTextRange(): TextRange? {
        return null //return null to avoid incorrect highlight at file start
    }

    override fun navigationRequest(): NavigationRequest? {
        return null //click to show usages
    }

    override fun navigate(requestFocus: Boolean) {
        //click to show usages
    }

    override fun canNavigate(): Boolean {
        return false //click to show usages
    }

    override fun getResolveScope(): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getResolveScope()
    }

    override fun getUseScope(): SearchScope {
        return ParadoxSearchScope.fromElement(this) ?: super.getUseScope()
    }

    override fun getPresentation(): ItemPresentation? {
        return Presentation(this)
    }

    class Presentation<T : MockPsiElement>(
        private val element: T
    ) : ItemPresentation {
        override fun getIcon(unused: Boolean) = element.icon

        override fun getPresentableText() = element.name
    }
}
