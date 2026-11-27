@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.refactoring.rename

import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxNameValidators
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class MarkdownInlineCodeRenameValidator(
    private val element: PsiElement,
) : RenameValidator {
    override fun validate(newName: String): RenameValidationResult {
        val valid = when {
            newName.isEmpty() -> false
            element is ParadoxScriptScriptedVariable -> ParadoxNameValidators.checkScriptedVariableName(newName)
            element is ParadoxLocalisationProperty -> ParadoxNameValidators.checkLocalisationPropertyName(newName)
            element is ParadoxDefinitionElement -> ParadoxNameValidators.checkDefinitionName(newName)
            else -> true
        }
        return if (valid) RenameValidationResult.ok() else RenameValidationResult.invalid()
    }
}
