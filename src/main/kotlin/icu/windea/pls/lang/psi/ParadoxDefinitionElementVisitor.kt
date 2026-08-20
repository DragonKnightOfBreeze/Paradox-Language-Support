package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * @see ParadoxDefinitionElement
 */
abstract class ParadoxDefinitionElementVisitor : PsiElementVisitor() {
    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element is ParadoxDefinitionElement) visitDefinitionElement(element)
    }

    open fun visitDefinitionElement(element: ParadoxDefinitionElement) {
        when (element) {
            is ParadoxScriptFile -> visitDefinitionElement(element)
            is ParadoxScriptProperty -> visitDefinitionElement(element)
        }
    }

    open fun visitFile(file: ParadoxScriptFile) {

    }

    open fun visitProperty(element: ParadoxScriptProperty) {

    }
}
