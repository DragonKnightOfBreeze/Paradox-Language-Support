package icu.windea.pls.core.psi

import com.intellij.navigation.PsiElementNavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.createPointer

open class BasePsiElementNavigationItem(
    element: NavigatablePsiElement,
    project: Project = element.project,
) : PsiElementNavigationItem {
    private val pointer = element.createPointer(project)
    private val _name = element.name
    private val _presentation = element.presentation

    override fun getTargetElement() = pointer.element
    override fun getName() = _name
    override fun getPresentation() = _presentation
}
