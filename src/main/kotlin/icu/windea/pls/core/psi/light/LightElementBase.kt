package icu.windea.pls.core.psi.light

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.light.LightElement

/**
 * 这类特殊的 [LightElement] 被设计为可以直接查找用法，或者导航到声明。
 */
abstract class LightElementBase(
    parent: PsiElement
) : LightElement(parent.manager, parent.language), NavigatablePsiElement {
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

    override fun isValid(): Boolean {
        return parent.isValid
    }

    override fun isWritable(): Boolean {
        return parent.isWritable
    }

    override fun isPhysical(): Boolean {
        return false
    }

    // default -> not provided
    override fun getTextRange(): TextRange? {
        return null
    }

    // default -> not provided
    override fun getTextOffset(): Int {
        return -1
    }

    // navigationElement = this -> ctrl + click to show usages
    // navigationElement = parent -> ctrl + click to navigate to parent element
    // override fun getNavigationElement(): PsiElement {
    //     return super.getNavigationElement()
    // }
}
