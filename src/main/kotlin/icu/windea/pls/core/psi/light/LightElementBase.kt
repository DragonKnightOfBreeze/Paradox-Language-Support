package icu.windea.pls.core.psi.light

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.light.LightElement
import javax.swing.Icon

@Suppress("UnstableApiUsage")
abstract class LightElementBase(
    parent: PsiElement
) : LightElement(parent.manager, parent.language), PsiNameIdentifierOwner, NavigatablePsiElement {
    private val myParent: PsiElement = parent

    override fun getParent(): PsiElement {
        return myParent
    }

    override fun getLanguage(): Language {
        return myParent.language
    }

    override fun getContainingFile(): PsiFile? {
        return myParent.containingFile
    }

    override fun getProject(): Project {
        return myManager.project
    }

    override fun isWritable(): Boolean {
        return true
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement? {
        return this
    }

    override fun getTextOffset(): Int {
        return 0
    }

    override fun getIcon(flags: Int): Icon? {
        return super.getIcon(flags)
    }

    override fun getTextRange(): TextRange? {
        return null
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
