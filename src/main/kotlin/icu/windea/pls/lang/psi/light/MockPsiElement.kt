package icu.windea.pls.lang.psi.light

import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.RenameableFakePsiElement

@Suppress("UnstableApiUsage")
abstract class MockPsiElement(parent: PsiElement) : RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement, ItemPresentation {
    override fun getParent(): PsiElement {
        return super.getParent()
    }

    override fun getLanguage(): Language {
        return parent.language
    }

    override fun getContainingFile(): PsiFile? {
        return parent.containingFile
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun getTextRange(): TextRange? {
        return null // return null to avoid incorrect highlight at file start
    }

    override fun navigationRequest(): NavigationRequest? {
        return null // click to show usages
    }

    override fun navigate(requestFocus: Boolean) {
        // click to show usages
    }

    override fun canNavigate(): Boolean {
        return false // click to show usages
    }
}
